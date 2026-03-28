package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.datastore.*
import com.rometools.rome.io.SyndFeedInput
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.StringReader
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

private const val RSSFeedSourceKIND = "RSSFeedSource"

fun Application.configureNewsRouting() {
    routing {
        // App Engine Cron endpoint
        get("/internal/cron/news-sync") {
            // App Engine cron tasks have this specific header assigned by Google.
            val isCron = call.request.headers["X-Appengine-Cron"] == "true"
            if (!isCron && call.request.headers["Host"]?.contains("localhost") != true) {
                call.application.log.warn("Unauthorized attempt to trigger cron sync")
                call.respond(
                        HttpStatusCode.Forbidden,
                        "Only App Engine Cron can access this endpoint"
                )
                return@get
            }

            // Run without blocking the HTTP response
            publishingScope.launch { syncNewsForAllSources(call.application) }

            call.respond(HttpStatusCode.Accepted, "Global RSS News sync started")
        }

        authenticate("firebase-auth") {
            post("/api/news/sync") {
                val user =
                        call.principal<User>()
                                ?: return@post call.respond(HttpStatusCode.Unauthorized)

                // Run without blocking the HTTP response
                publishingScope.launch { syncNewsForUser(user.userId, call.application) }

                call.respond(
                        HttpStatusCode.Accepted,
                        mapOf("status" to "RSS News sync started for user ${user.userId}")
                )
            }
        }
    }
}

suspend fun syncNewsForAllSources(application: Application) {
    withContext(Dispatchers.IO) {
        try {
            val datastore = DatastoreOptions.getDefaultInstance().service

            // Retrieve all RSS Sources in the application
            val query = Query.newEntityQueryBuilder().setKind(RSSFeedSourceKIND).build()
            val sources = datastore.run(query)

            val sourceEntities = mutableListOf<Entity>()
            while (sources.hasNext()) {
                sourceEntities.add(sources.next())
            }

            application.log.info("Cron: Found ${sourceEntities.size} sources to sync")
            if (!sourceEntities.isEmpty()) {

                // Create a channel to act as a work queue
                val sourceChannel = Channel<Entity>(Channel.UNLIMITED)
                sourceEntities.forEach { sourceChannel.trySend(it) }
                sourceChannel.close()

                // Launch a pool of exactly 20 worker coroutines
                val workers =
                        List(20) {
                            launch {
                                for (source in sourceChannel) {
                                    processSourcesAndSaveNews(
                                            datastore,
                                            listOf(source),
                                            application
                                    )
                                }
                            }
                        }
                workers.joinAll()

                deleteOldRSSNews(datastore, application)
            }
        } catch (e: Exception) {
            application.log.error("Error during global news sync", e)
        }
    }
}

suspend fun syncNewsForUser(userId: String, application: Application) {
    withContext(Dispatchers.IO) {
        try {
            val datastore = DatastoreOptions.getDefaultInstance().service

            // Filter sources only strictly for the triggered user
            val query =
                    Query.newEntityQueryBuilder()
                            .setKind(RSSFeedSourceKIND)
                            .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
                            .build()

            val sources = datastore.run(query)
            val sourceEntities = mutableListOf<Entity>()
            while (sources.hasNext()) {
                sourceEntities.add(sources.next())
            }

            application.log.info("User $userId: Found ${sourceEntities.size} sources to sync")
            if (!sourceEntities.isEmpty()) {
                // Create a channel to act as a work queue
                val sourceChannel = Channel<Entity>(Channel.UNLIMITED)
                sourceEntities.forEach { sourceChannel.trySend(it) }
                sourceChannel.close()

                // Launch a pool of exactly 20 worker coroutines
                val workers =
                        List(20) {
                            launch {
                                for (source in sourceChannel) {
                                    processSourcesAndSaveNews(
                                            datastore,
                                            listOf(source),
                                            application
                                    )
                                }
                            }
                        }
                workers.joinAll()

                deleteOldRSSNews(datastore, application)
            }
        } catch (e: Exception) {
            application.log.error("Error during user news sync", e)
        }
    }
}

// Define your client with a default User-Agent
private val httpClient = HttpClient {
    install(io.ktor.client.plugins.UserAgent) {
        agent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }

    install(ContentEncoding) {
        gzip()
        deflate()
    }

    // 3. Set a reasonable timeout so one dead feed doesn't hang a worker
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000 // 15 seconds
        connectTimeoutMillis = 5_000 // 5 seconds
    }
}

private suspend fun processSourcesAndSaveNews(
        datastore: Datastore,
        sourceEntities: List<Entity>,
        application: Application
) {
    val entitiesToSave = mutableListOf<Entity>()
    
    for (source in sourceEntities) {
        val sourceId = source.key.nameOrId.toString()
        val userId = if (source.contains("userId")) source.getString("userId") else continue
        val url = if (source.contains("url")) source.getString("url") else continue
        val tagsStr = if (source.contains("tags")) source.getString("tags") else ""
        val tags = tagsStr.split(" ", ",").filter { it.isNotBlank() }

        application.log.info("Syncing RSS for source URL: $url")
        var syncStatus = "SUCCESS"

        try {
            // Fetch the feed
            val response: HttpResponse =
                    httpClient.get(url) {
                        // Signal to the server that we prefer XML formats
                        header(
                                HttpHeaders.Accept,
                                "application/rss+xml, application/atom+xml, text/xml"
                        )
                    }

            if (response.status.value !in 200..299) {
                throw Exception("HTTP ${response.status.value}: ${response.status.description}")
            }

            val bodyString = response.bodyAsText()

            // Offload CPU-heavy parsing to the Default dispatcher
            val fetchedArticles =
                    withContext(Dispatchers.Default) {
                        val input = SyndFeedInput()
                        val feed = input.build(StringReader(bodyString))

                        feed.entries.map { entry ->
                            ParsedArticle(
                                    title = entry.title?.trim() ?: "Untitled",
                                    url = entry.link?.trim() ?: "",
                                    publishedAt = entry.publishedDate?.toInstant() ?: Instant.now()
                            )
                        }
                    }

                // Fetch existing URLs for this source to avoid N+1 querying
                val existingUrls = mutableSetOf<String>()
                val existingQuery = Query.newEntityQueryBuilder()
                        .setKind("RSSNews")
                        .setFilter(StructuredQuery.PropertyFilter.eq("sourceId", sourceId))
                        .build()
                val results = datastore.run(existingQuery)
                while (results.hasNext()) {
                    val entity = results.next()
                    if (entity.contains("url")) {
                        existingUrls.add(entity.getString("url"))
                    }
                }

            // Save articles back on the current thread (already in IO from parent)
            for (article in fetchedArticles) {
                    if (!existingUrls.contains(article.url)) {
                        val entity = createArticleEntity(datastore, sourceId, userId, article, tags)
                        entitiesToSave.add(entity)
                        existingUrls.add(article.url) // Prevent duplicates in the same feed
                    }
            }
        } catch (e: Exception) {
            application.log.error("Failed sync for $url: ${e.message}")
            syncStatus = "ERROR"
        } finally {
            // Ensure the source entity is updated even on failure
            val updatedSource =
                    Entity.newBuilder(source)
                            .set("lastSyncTime", com.google.cloud.Timestamp.now())
                            .set("syncStatus", syncStatus)
                            .build()
                entitiesToSave.add(updatedSource)
        }
    }

    if (entitiesToSave.isNotEmpty()) {
        entitiesToSave.chunked(500).forEach { batch -> datastore.put(*batch.toTypedArray()) }
    }
}

private fun createArticleEntity(
        datastore: Datastore,
        sourceId: String,
        userId: String,
        article: ParsedArticle,
        defaultTags: List<String>
): Entity {
    // Create new RSSNews entity mapped with defaults
    val key = datastore.newKeyFactory().setKind("RSSNews").newKey(UUID.randomUUID().toString())
    val timestamp = article.publishedAt.toEpochMilli()

    val entityBuilder =
            Entity.newBuilder(key)
                    .set("sourceId", sourceId)
                    .set("userId", userId)
                    .set("title", article.title)
                    .set("url", article.url)
                    .set("publishedAt", timestamp)
                    .set("read", false)
                    .set("comments", "")

    // Map tags array to a Datastore ListValue
    val tagsListValue = ListValue.newBuilder()
    defaultTags.forEach { tagsListValue.addValue(it) }
    entityBuilder.set("tags", tagsListValue.build())

    return entityBuilder.build()
}

private fun deleteOldRSSNews(datastore: Datastore, application: Application) {
    // Clean entries strictly older than 1 month
    val oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS)
    val timestamp =
            com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    oneMonthAgo.epochSecond,
                    oneMonthAgo.nano
            )

    val query =
            Query.newKeyQueryBuilder()
                    .setKind("RSSNews")
                    .setFilter(StructuredQuery.PropertyFilter.lt("publishedAt", timestamp))
                    .build()

    val results = datastore.run(query)
    val keysToDelete = mutableListOf<Key>()
    while (results.hasNext()) {
        keysToDelete.add(results.next())
    }

    if (keysToDelete.isNotEmpty()) {
        application.log.info("Cleaning up ${keysToDelete.size} old RSSNews entries")

        // Datastore limits batch operations to 500 keys, so we batch them.
        keysToDelete.chunked(500).forEach { batch -> datastore.delete(*batch.toTypedArray()) }
    }
}

// Extension to safely extract a Datastore Key's name or auto-incremented ID
val Key.nameOrId: Any
    get() = name ?: id

// Data class to represent a parsed article block extracted from an RSS feed
data class ParsedArticle(val title: String, val url: String, val publishedAt: Instant)

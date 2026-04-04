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
import io.ktor.server.request.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.StringReader
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.net.URI
import com.google.auth.ServiceAccountSigner
import com.google.auth.oauth2.GoogleCredentials

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

            call.application.log.info("Cron job started: Syncing global news feeds...")

            try {
                publishingScope.launch { syncNewsForAllSources(call.application) }
                call.respond(HttpStatusCode.Accepted, mapOf("status" to "RSS News sync started"))
                
            } catch (e: Exception) {
                // It's good practice to catch fatal errors so the Cron engine records a failure
                call.application.log.error("Cron job failed during global news sync", e)
                call.respond(HttpStatusCode.InternalServerError, "Global RSS News sync failed")
            }
        }

        authenticate("firebase-auth") {
            get("/api/news") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    val datastore = DatastoreOptions.getDefaultInstance().service
                    val unreadOnly = call.request.queryParameters["unreadOnly"]?.toBoolean() ?: false

                    val filter = if (unreadOnly) {
                        StructuredQuery.CompositeFilter.and(
                            StructuredQuery.PropertyFilter.eq("userId", user.userId),
                            StructuredQuery.PropertyFilter.eq("read", false)
                        )
                    } else {
                        StructuredQuery.PropertyFilter.eq("userId", user.userId)
                    }

                    val query = Query.newEntityQueryBuilder()
                        .setKind("RSSNews")
                        .setFilter(filter)
                        .setOrderBy(StructuredQuery.OrderBy.desc("publishedAt"))
                        .build()

                    val results = datastore.run(query)
                    val newsList = mutableListOf<NewsItem>()

                    while (results.hasNext()) {
                        val entity = results.next()
                        val tagsList = if (entity.contains("tags")) {
                            entity.getList<Value<*>>("tags").map { it.get().toString() }
                        } else emptyList()

                        val rawUrl = if (entity.contains("imageUrl")) entity.getString("imageUrl") else ""

                        val imageUrl = try {
                            // Check if the URL from the DB matches one of the values in your map
                            if (defaultCategoryImages.containsValue(rawUrl)) {
                                // If you implemented the cache service, call cacheService.getValidSignedUrl(rawUrl) here instead!
                                ImageUrlSignerService.generateSignedImageUrl(rawUrl) 
                            } else {
                                // It's not a default Google Cloud Storage image (maybe an external link), 
                                // so just return it as-is.
                                rawUrl 
                            }
                        } catch (e: Exception) {
                            // If signing fails, fallback to the raw URL
                            call.application.log.error("Error signing image URL: $e")
                            rawUrl 
                        }

                        newsList.add(
                            NewsItem(
                                id = entity.key.nameOrId.toString(),
                                sourceId = if (entity.contains("sourceId")) entity.getString("sourceId") else "",
                                title = if (entity.contains("title")) entity.getString("title") else "",
                                url = if (entity.contains("url")) entity.getString("url") else "",
                                imageUrl = imageUrl,
                                summary = if (entity.contains("summary")) entity.getString("summary") else "",
                                publishedAt = if (entity.contains("publishedAt")) entity.getTimestamp("publishedAt").seconds else 0L,
                                read = if (entity.contains("read")) entity.getBoolean("read") else false,
                                comments = if (entity.contains("comments")) entity.getString("comments") else "",
                                tags = tagsList
                            )
                        )
                    }

                    call.respond(newsList)
                } catch (e: Exception) {
                    call.application.log.error("Error fetching news for user ${user.userId}", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }

            post("/api/news/sync") {
                val user = call.principal<User>() ?: return@post call.respond(HttpStatusCode.Unauthorized)

                // Run without blocking the HTTP response
                publishingScope.launch { syncNewsForUser(user.userId, call.application) }

                call.respond(
                    HttpStatusCode.Accepted,
                    mapOf("status" to "RSS News sync started for user ${user.userId}")
                )
            }

                put("/api/news/read") {
                    val user = call.principal<User>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                    val request = try {
                        call.receive<UpdateReadStatusRequest>()
                    } catch (e: Exception) {
                        return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                    }

                    try {
                        val datastore = DatastoreOptions.getDefaultInstance().service
                        val keyFactory = datastore.newKeyFactory().setKind("RSSNews")
                        val keys = request.newsIds.map { keyFactory.newKey(it) }

                        val entities = datastore.get(keys)
                        val updatedEntities = mutableListOf<Entity>()

                        while (entities.hasNext()) {
                            val entity = entities.next()
                            if (entity.contains("userId") && entity.getString("userId") == user.userId) {
                                val updated = Entity.newBuilder(entity)
                                    .set("read", request.read)
                                    .build()
                                updatedEntities.add(updated)
                            }
                        }

                        if (updatedEntities.isNotEmpty()) {
                            updatedEntities.chunked(500).forEach { batch ->
                                datastore.put(*batch.toTypedArray())
                            }
                        }

                        call.respond(HttpStatusCode.OK, mapOf("status" to "success", "updatedCount" to updatedEntities.size.toString()))
                    } catch (e: Exception) {
                        call.application.log.error("Error updating read status for user ${user.userId}", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                    }
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

            // 1. Give the channel a reasonable capacity (e.g., 100) instead of UNLIMITED
            val sourceChannel = Channel<Entity>(capacity = 100)

            // 2. Launch a "Producer" coroutine to feed the channel directly from the DB
            // This replaces the dangerous mutableListOf
            launch {
                var count = 0
                while (sources.hasNext()) {
                    sourceChannel.send(sources.next()) 
                    count++
                }
                application.log.info("Cron: Found and queued $count sources to sync")
                sourceChannel.close() 
            }

            // 3. Launch the 20 "Consumer" workers
            val workers = List(20) {
                launch {
                    for (source in sourceChannel) {
                        processSourceAndSaveNews(datastore, source, application)
                    }
                }
            }
            
            workers.joinAll()
            
            deleteOldRSSNews(datastore, application)

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
            val query = Query.newEntityQueryBuilder()
                .setKind(RSSFeedSourceKIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
                .build()

            val sources = datastore.run(query)
            val sourceEntities = mutableListOf<Entity>()
            while (sources.hasNext()) {
                sourceEntities.add(sources.next())
            }

            application.log.info("User $userId: Found ${sourceEntities.size} sources to sync")
            if (sourceEntities.isNotEmpty()) {
                // Create a channel to act as a work queue
                val sourceChannel = Channel<Entity>(Channel.UNLIMITED)
                sourceEntities.forEach { sourceChannel.trySend(it) }
                sourceChannel.close()

                // Launch a pool of exactly 20 worker coroutines
                val workers = List(20) {
                    launch {
                        for (source in sourceChannel) {
                            processSourceAndSaveNews(datastore, source, application)
                        }
                    }
                }
                workers.joinAll()
            }

            deleteOldRSSNews(datastore, application)

        } catch (e: Exception) {
            application.log.error("Error during user news sync", e)
        }
    }
}

// Define your client with a default User-Agent
private val httpClient = HttpClient {
    install(io.ktor.client.plugins.UserAgent) {
        agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
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

private suspend fun processSourceAndSaveNews(
    datastore: Datastore,
    source: Entity,
    application: Application
) {

    val sourceId = source.key.nameOrId.toString()
    val userId = if (source.contains("userId")) source.getString("userId") else return
    val url = if (source.contains("url")) source.getString("url") else return
    
    val tagsStr = if (source.contains("tags")) source.getString("tags") else ""
    val tags = tagsStr.split(" ", ",").filter { it.isNotBlank() }

    application.log.info("Syncing RSS for source URL: $url")
    var syncStatus = "SUCCESS"
    val entitiesToSave = mutableListOf<Entity>()

    try {

        // Fetch the feed
        val response: HttpResponse = httpClient.get(url) {
            header(
                HttpHeaders.Accept,
                "application/rss+xml, application/atom+xml, text/xml"
            )
        }

        if (response.status.value !in 200..299) {
            throw Exception("HTTP ${response.status.value}: ${response.status.description}")
        }

        val bodyString = response.bodyAsText()

        // 2. Prevent Write Amplification: Calculate a smart cutoff date
        val lastSyncRaw = if (source.contains("lastSyncTime")) source.getTimestamp("lastSyncTime")?.toDate()?.toInstant() else null
        val baselineCutoff = Instant.now().minus(AppConfig.rssNewsBaselineCutoffDays, ChronoUnit.DAYS)
        
        // Use lastSyncTime if it exists and is newer than the configured days ago; otherwise fallback.
        val cutoffDate = if (lastSyncRaw != null && lastSyncRaw.isAfter(baselineCutoff)) {
            lastSyncRaw
        } else {
            baselineCutoff
        }

        // Offload CPU-heavy parsing
        val fetchedArticles = withContext(Dispatchers.Default) {
            val input = SyndFeedInput()
            val feed = input.build(StringReader(bodyString))

            feed.entries.mapNotNull { entry ->

                val publishedAt = entry.publishedDate?.toInstant() ?: Instant.now()

                if (publishedAt.isBefore(cutoffDate)) {
                    return@mapNotNull null 
                }

                val extractedTags = entry.categories
                    ?.mapNotNull { it.name?.trim()?.lowercase() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                val combinedTagsForImage = (tags + extractedTags).distinct()

                val extractedImageUrl = entry.enclosures
                    ?.firstOrNull { it.type?.startsWith("image/") == true }
                    ?.url

                val imageUrl = determineImageUrl(extractedImageUrl, combinedTagsForImage)   

                val rawDescription = entry.description?.value ?: ""
    
                // Strip HTML tags using Regex and remove excessive whitespace
                val noHtmlDescription = rawDescription
                    .replace(Regex("<[^>]*>"), "") // Removes <p>, <a>, <img>, etc.
                    .replace(Regex("\\s+"), " ")   // Condenses multiple spaces/newlines into one
                    .trim()
        
                // Truncate to 250 characters, adding an ellipsis if it's longer
                val finalSummary = if (noHtmlDescription.length > 250) {
                    noHtmlDescription.take(247) + "..."
                } else {
                    noHtmlDescription
                }
                
                ParsedArticle(
                    title = entry.title?.trim() ?: "Untitled",
                    url = entry.link?.trim() ?: "",
                    publishedAt = publishedAt,
                    imageUrl = imageUrl,
                    articleTags = extractedTags,
                    summary = finalSummary
                )
            }
        }

        // Prepare new articles for saving
        for (article in fetchedArticles) {
            val entity = createArticleEntity(datastore, sourceId, userId, article, tags)
            entitiesToSave.add(entity)
            
        }
        
    } catch (e: Exception) {
        application.log.error("Failed sync for $url: ${e.message}")
        syncStatus = "ERROR"
    } finally {
        // Ensure the source entity is updated even on failure
        val updatedSource = Entity.newBuilder(source)
            .set("lastSyncTime", com.google.cloud.Timestamp.now())
            .set("syncStatus", syncStatus)
            .build()
            
        entitiesToSave.add(updatedSource)
    }

    // Save everything in one batch on the IO dispatcher
    if (entitiesToSave.isNotEmpty()) {
        withContext(Dispatchers.IO) {
            entitiesToSave.chunked(500).forEach { batch -> 
                datastore.put(*batch.toTypedArray()) 
            }
        }
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
    val urlHash = article.url.toSha256()
    val key = datastore.newKeyFactory().setKind("RSSNews").newKey(urlHash)

    val timestamp = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
        article.publishedAt.epochSecond,
        article.publishedAt.nano
    )

    val entityBuilder = Entity.newBuilder(key)
        .set("sourceId", sourceId)
        .set("userId", userId)
        .set("title", article.title)
        .set("url", article.url)
        .set("imageUrl", article.imageUrl)
        .set("summary", article.summary)
        .set("publishedAt", timestamp)
        .set("read", false)
        .set("comments", article.articleTags.joinToString(", "))

    // Map tags array to a Datastore ListValue
    val tagsListValue = ListValue.newBuilder()
    defaultTags.forEach { tagsListValue.addValue(it) }
    entityBuilder.set("tags", tagsListValue.build())

    return entityBuilder.build()
}

private fun deleteOldRSSNews(datastore: Datastore, application: Application) {
    // Clean entries strictly older than the configured retention period
    val deletionThreshold = Instant.now().minus(AppConfig.rssNewsRetentionDays, ChronoUnit.DAYS)
    
    // CHANGE HERE: Match the exact Datastore data type we used to save the entity
    val timestampLimit = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
        deletionThreshold.epochSecond,
        deletionThreshold.nano
    )

    val query = Query.newKeyQueryBuilder()
        .setKind("RSSNews")
        .setFilter(StructuredQuery.PropertyFilter.lt("publishedAt", timestampLimit))
        .build()

    val results = datastore.run(query)
    val keysToDelete = mutableListOf<Key>()
    while (results.hasNext()) {
        keysToDelete.add(results.next())
    }

    if (keysToDelete.isNotEmpty()) {
        application.log.info("Cleaning up ${keysToDelete.size} old RSSNews entries")
        keysToDelete.chunked(500).forEach { batch -> datastore.delete(*batch.toTypedArray()) }
    }
}

// Extension to safely extract a Datastore Key's name or auto-incremented ID
val Key.nameOrId: Any
    get() = name ?: id

// Data class to represent a parsed article block extracted from an RSS feed
data class ParsedArticle(
    val title: String, 
    val url: String, 
    val publishedAt: Instant,
    val imageUrl: String,
    val articleTags: List<String> = emptyList(),
    val summary: String = "" 
)
private val defaultCategoryImages = mapOf(
    "artificial intelligence" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Artificial%20Intelligence.jpg",
    "software" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Software.jpg",
    "open source" to "https://storage.cloud.google.com/cathartic_computer_club/Images/OpenSource.jpg",
    "startups" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Startups.jpg",
    "science" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Science.jpg",
    "engineering" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Engineering.jpg",
    "misc" to "https://storage.cloud.google.com/cathartic_computer_club/Images/Misc.jpg"
)

private fun determineImageUrl(parsedImageUrl: String?, tags: List<String>): String {
    // If the RSS feed ACTUALLY provided a valid image, you might still want to use it.
    if (!parsedImageUrl.isNullOrBlank()) {
        return parsedImageUrl
    }

    // Normalize tags to lowercase for safe matching
    val normalizedTags = tags.map { it.lowercase() }

    // Find the first tag that matches one of our defined categories
    for (tag in normalizedTags) {
        if (defaultCategoryImages.containsKey(tag)) {
            return defaultCategoryImages[tag]!!
        }
    }

    // Fallback if no matching tags are found
    return defaultCategoryImages["misc"]!!
}

fun String.toSha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

@Serializable
data class NewsItem(
    val id: String,
    val sourceId: String,
    val title: String,
    val url: String,
    val imageUrl: String,
    val summary: String,
    val publishedAt: Long,
    val read: Boolean,
    val comments: String,
    val tags: List<String>
)

@Serializable
data class UpdateReadStatusRequest(
    val newsIds: List<String>,
    val read: Boolean
)
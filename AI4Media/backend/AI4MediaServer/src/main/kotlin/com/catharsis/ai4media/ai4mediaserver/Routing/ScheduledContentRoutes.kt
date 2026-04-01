package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.content.*
import com.google.cloud.datastore.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

fun Application.configureScheduledContentRouting() {
    routing {
        authenticate("firebase-auth") {
            get("/api/scheduled") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                try {
                    val datastore = DatastoreOptions.getDefaultInstance().service
                    
                    // Query all SocialContent for the current user, ordered by scheduled time
                    val query = Query.newEntityQueryBuilder()
                        .setKind("SocialContent")
                        .setFilter(StructuredQuery.PropertyFilter.eq("userId", user.userId))
                        .setOrderBy(StructuredQuery.OrderBy.asc("scheduledTime"))
                        .build()

                    val results = datastore.run(query)
                    val scheduledList = mutableListOf<SocialContent>()

                    while (results.hasNext()) {
                        val entity = results.next()
                        val status = if (entity.contains("status")) entity.getString("status") else ""
                        
                        // Filter in-memory for active scheduled posts
                        if (status == PostStatus.SCHEDULED.name || status == PostStatus.AUTOSCHEDULED.name) {
                            val tagsList = if (entity.contains("tags")) {
                                entity.getList<Value<*>>("tags").map { it.get().toString() }
                            } else emptyList()

                            val scheduledTime = if (entity.contains("scheduledTime")) {
                                val ts = entity.getTimestamp("scheduledTime")
                                LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                            } else LocalDateTime.now(AppConfig.timeZone)

                            val createdTime = if (entity.contains("createdTime")) {
                                val ts = entity.getTimestamp("createdTime")
                                LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                            } else LocalDateTime.now(AppConfig.timeZone)

                            val idStr = entity.key.nameOrId.toString()
                            val id = try { UUID.fromString(idStr) } catch (e: Exception) { UUID.randomUUID() }

                            val networkStr = if (entity.contains("network")) entity.getString("network") else ""
                            val network = try { SocialNetwork.valueOf(networkStr.uppercase()) } catch (e: Exception) { SocialNetwork.LINKEDIN }

                            val postStatus = try { PostStatus.valueOf(status) } catch (e: Exception) { PostStatus.DRAFT }

                            scheduledList.add(
                                SocialContent(
                                    id = id,
                                    userId = if (entity.contains("userId")) entity.getString("userId") else "",
                                    textContent = if (entity.contains("textContent")) entity.getString("textContent") else "",
                                    urlContent = if (entity.contains("urlContent")) entity.getString("urlContent") else "",
                                    targetUrn = if (entity.contains("targetUrn")) entity.getString("targetUrn") else null,
                                    scheduledTime = scheduledTime,
                                    createdTime = createdTime,
                                    firstComment = if (entity.contains("firstComment")) entity.getString("firstComment") else null,
                                    tags = tagsList,
                                    status = postStatus,
                                    network = network
                                )
                            )
                        }
                    }

                    call.respond(scheduledList)
                } catch (e: Exception) {
                    call.application.log.error("Error fetching scheduled content for user ${user.userId}", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }

            get("/api/published") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val daysParam = call.request.queryParameters["days"]?.toLongOrNull() ?: 30L
                
                try {
                    val datastore = DatastoreOptions.getDefaultInstance().service
                    
                    // Calculate the cutoff date for the last 'n' days
                    val cutoffTime = LocalDateTime.now(AppConfig.timeZone).minusDays(daysParam)
                    val cutoffInstant = cutoffTime.atZone(AppConfig.timeZone).toInstant()
                    val timestampLimit = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                        cutoffInstant.epochSecond,
                        cutoffInstant.nano
                    )

                    // Query SocialContent for the user, starting from the cutoff date, ordered descending
                    val query = Query.newEntityQueryBuilder()
                        .setKind("SocialContent")
                        .setFilter(
                            StructuredQuery.CompositeFilter.and(
                                StructuredQuery.PropertyFilter.eq("userId", user.userId),
                                StructuredQuery.PropertyFilter.ge("scheduledTime", timestampLimit)
                            )
                        )
                        .setOrderBy(StructuredQuery.OrderBy.desc("scheduledTime"))
                        .build()

                    val results = datastore.run(query)
                    val publishedList = mutableListOf<SocialContent>()

                    while (results.hasNext()) {
                        val entity = results.next()
                        val status = if (entity.contains("status")) entity.getString("status") else ""
                        
                        // Filter in-memory for published or failed posts
                        if (status == PostStatus.PUBLISHED.name || status == PostStatus.FAILED.name) {
                            val tagsList = if (entity.contains("tags")) {
                                entity.getList<Value<*>>("tags").map { it.get().toString() }
                            } else emptyList()

                            val scheduledTime = if (entity.contains("scheduledTime")) {
                                val ts = entity.getTimestamp("scheduledTime")
                                LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                            } else LocalDateTime.now(AppConfig.timeZone)

                            val createdTime = if (entity.contains("createdTime")) {
                                val ts = entity.getTimestamp("createdTime")
                                LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                            } else LocalDateTime.now(AppConfig.timeZone)

                            val idStr = entity.key.nameOrId.toString()
                            val id = try { UUID.fromString(idStr) } catch (e: Exception) { UUID.randomUUID() }

                            val networkStr = if (entity.contains("network")) entity.getString("network") else ""
                            val network = try { SocialNetwork.valueOf(networkStr.uppercase()) } catch (e: Exception) { SocialNetwork.LINKEDIN }

                            val postStatus = try { PostStatus.valueOf(status) } catch (e: Exception) { PostStatus.DRAFT }

                            publishedList.add(
                                SocialContent(
                                    id = id,
                                    userId = if (entity.contains("userId")) entity.getString("userId") else "",
                                    textContent = if (entity.contains("textContent")) entity.getString("textContent") else "",
                                    urlContent = if (entity.contains("urlContent")) entity.getString("urlContent") else "",
                                    targetUrn = if (entity.contains("targetUrn")) entity.getString("targetUrn") else null,
                                    scheduledTime = scheduledTime,
                                    createdTime = createdTime,
                                    firstComment = if (entity.contains("firstComment")) entity.getString("firstComment") else null,
                                    tags = tagsList,
                                    status = postStatus,
                                    network = network
                                )
                            )
                        }
                    }

                    call.respond(publishedList)
                } catch (e: Exception) {
                    call.application.log.error("Error fetching published content for user ${user.userId}", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }
        }
    }
}

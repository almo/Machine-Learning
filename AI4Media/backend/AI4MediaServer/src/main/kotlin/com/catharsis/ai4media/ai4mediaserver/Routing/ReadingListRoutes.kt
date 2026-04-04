package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.datastore.*
import com.google.cloud.Timestamp
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import com.catharsis.ai4media.ai4mediaserver.model.ZonedDateTimeSerializer

@Serializable
data class ReadingListItem(
    val id: String? = null,
    val title: String,
    val url: String,
    val comments: String? = "",
    @Serializable(with = ZonedDateTimeSerializer::class) val createdAt: ZonedDateTime? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val newsDate: ZonedDateTime? = null
)

@Serializable
data class ReadingListCreateRequest(
    val title: String,
    val url: String,
    val comments: String? = "",
    @Serializable(with = ZonedDateTimeSerializer::class) val newsDate: ZonedDateTime? = null
)

fun Application.configureReadingListRouting() {
    val datastore = DatastoreOptions.getDefaultInstance().service
    val kind = "ReadingList"

    routing {
        authenticate("firebase-auth") {
            
            // GET: Retrieve all reading list items for the authenticated user
            get("/api/reading-list") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    val query = Query.newEntityQueryBuilder()
                        .setKind(kind)
                        .setFilter(StructuredQuery.PropertyFilter.eq("userId", user.userId))
                        .setOrderBy(StructuredQuery.OrderBy.desc("createdAt"))
                        .build()

                    val results = datastore.run(query)
                    val items = mutableListOf<ReadingListItem>()

                    while (results.hasNext()) {
                        val entity = results.next()
                        items.add(
                            ReadingListItem(
                                id = entity.key.nameOrId.toString(),
                                title = if (entity.contains("title")) entity.getString("title") else "",
                                url = if (entity.contains("url")) entity.getString("url") else "",
                                comments = if (entity.contains("comments")) entity.getString("comments") else "",
                                createdAt = if (entity.contains("createdAt") && !entity.isNull("createdAt")) {
                                    try {
                                        val ts = entity.getTimestamp("createdAt")
                                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                                    } catch (e: Exception) {
                                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(entity.getLong("createdAt")), AppConfig.timeZone)
                                    }
                                } else null,
                                newsDate = if (entity.contains("newsDate") && !entity.isNull("newsDate")) {
                                    try {
                                        val ts = entity.getTimestamp("newsDate")
                                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()), AppConfig.timeZone)
                                    } catch (e: Exception) {
                                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(entity.getLong("newsDate")), AppConfig.timeZone)
                                    }
                                } else null
                            )
                        )
                    }
                    call.respond(HttpStatusCode.OK, items)
                } catch (e: Exception) {
                    call.application.log.error("Error fetching reading list", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }

            // POST: Add a new item to the reading list
            post("/api/reading-list") {
                val user = call.principal<User>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<ReadingListCreateRequest>()
                    val keyFactory = datastore.newKeyFactory().setKind(kind)
                    val key = datastore.allocateId(keyFactory.newKey()) // Auto-generates a unique ID

                    val entityBuilder = Entity.newBuilder(key)
                        .set("userId", user.userId)
                        .set("title", request.title)
                        .set("url", request.url)
                        .set("comments", request.comments ?: "")
                        .set("createdAt", Timestamp.now())
                        
                    request.newsDate?.let { entityBuilder.set("newsDate", Timestamp.ofTimeSecondsAndNanos(it.toEpochSecond(), it.nano)) }

                    datastore.put(entityBuilder.build())
                    
                    call.respond(HttpStatusCode.Created, mapOf("id" to key.id.toString(), "status" to "success"))
                } catch (e: Exception) {
                    call.application.log.error("Error adding to reading list", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }

            // PUT: Update an existing item (e.g. update comments)
            put("/api/reading-list/{id}") {
                val user = call.principal<User>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val idStr = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing ID")
                
                try {
                    val request = call.receive<ReadingListCreateRequest>()
                    val keyFactory = datastore.newKeyFactory().setKind(kind)
                    val key = keyFactory.newKey(idStr.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID Format"))
                    
                    val existingEntity = datastore.get(key)
                    if (existingEntity == null || existingEntity.getString("userId") != user.userId) {
                        return@put call.respond(HttpStatusCode.NotFound, "Item not found")
                    }

                    val updatedEntity = Entity.newBuilder(existingEntity)
                        .set("title", request.title)
                        .set("url", request.url)
                        .set("comments", request.comments ?: "")
                        .build()

                    datastore.put(updatedEntity)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
                } catch (e: Exception) {
                    call.application.log.error("Error updating reading list item", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }

            // DELETE: Remove an item from the reading list
            delete("/api/reading-list/{id}") {
                val user = call.principal<User>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val idStr = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing ID")
                
                try {
                    val keyFactory = datastore.newKeyFactory().setKind(kind)
                    val key = keyFactory.newKey(idStr.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID Format"))
                    
                    val existingEntity = datastore.get(key)
                    if (existingEntity == null || existingEntity.getString("userId") != user.userId) {
                        return@delete call.respond(HttpStatusCode.NotFound, "Item not found")
                    }

                    datastore.delete(key)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
                } catch (e: Exception) {
                    call.application.log.error("Error deleting reading list item", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
                }
            }
        }
    }
}
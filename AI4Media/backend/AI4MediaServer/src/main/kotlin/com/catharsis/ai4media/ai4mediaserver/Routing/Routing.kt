package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.content.*
import com.google.cloud.Timestamp
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StringValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*

val publishingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

data class UserScheduleSettings(
    val dailyLimits: Map<SocialNetwork, Int>,
    val sweetSpots: List<Pair<LocalTime, LocalTime>>
)

object UserSettingsRegistry {
    val defaultSettings = UserScheduleSettings(
        dailyLimits = mapOf(
            SocialNetwork.TWITTER to 5,
            SocialNetwork.LINKEDIN to 2
        ),
        sweetSpots = listOf(
            LocalTime.of(7, 30) to LocalTime.of(9, 30),   // Morning
            LocalTime.of(12, 30) to LocalTime.of(14, 0),  // Lunch Break
            LocalTime.of(17, 30) to LocalTime.of(19, 30), // Returning Home
            LocalTime.of(21, 0) to LocalTime.of(22, 30)   // Night
        )
    )

    // In-memory mapping for user-specific settings. 
    // Defaults are used if a user hasn't configured custom settings.
    private val userSettings = mutableMapOf<String, UserScheduleSettings>()

    fun getSettingsForUser(userId: String): UserScheduleSettings {
        return userSettings[userId] ?: defaultSettings
    }
}

/**
 * Calculates the optimal scheduling time for a social media post based on sweet spots, daily limits, 
 * and existing auto-scheduled posts.
 * 
 * @param network The target social network.
 * @param settings The schedule settings for the user (limits, sweet spots).
 * @param futureAutoScheduledPosts A list of LocalDateTime representing all currently future AUTOSCHEDULED posts for this user and network.
 * @return A LocalDateTime representing the calculated optimized schedule time.
 */
fun calculateOptimizedScheduleTime(
    network: SocialNetwork,
    settings: UserScheduleSettings,
    futureAutoScheduledPosts: List<LocalDateTime>
): LocalDateTime {
    val now = LocalDateTime.now(AppConfig.timeZone)
    val lastPostTime = futureAutoScheduledPosts.maxOrNull() ?: now
    var currentDate = lastPostTime.toLocalDate()

    val dailyLimit = settings.dailyLimits[network] ?: 2
    val spots = settings.sweetSpots

    while (true) {
        val postsOnDate = futureAutoScheduledPosts.filter { it.toLocalDate() == currentDate }
        
        if (postsOnDate.size < dailyLimit) {
            for (spot in spots) {
                val spotStart = LocalDateTime.of(currentDate, spot.first)
                val spotEnd = LocalDateTime.of(currentDate, spot.second)
                
                // Skip if spot is already completely in the past
                if (!spotEnd.isAfter(now)) continue
                
                // Enforce sequential scheduling: the spot must end after the latest scheduled post
                if (!spotEnd.isAfter(lastPostTime)) continue
                
                // Check if the spot is already occupied by any existing auto-scheduled post
                val isOccupied = postsOnDate.any { it >= spotStart && it <= spotEnd }
                
                if (!isOccupied) {
                    // Sweet spot found! Calculate an effective start in case we are currently inside it.
                    val effectiveStart = if (spotStart.isBefore(now)) now else spotStart
                    val secondsBetween = ChronoUnit.SECONDS.between(effectiveStart, spotEnd)
                    
                    val randomSeconds = if (secondsBetween > 0) (0..secondsBetween).random() else 0L
                    return effectiveStart.plusSeconds(randomSeconds)
                }
            }
        }
        
        // Roll over to the next calendar day
        currentDate = currentDate.plusDays(1)
    }
}

@Serializable data class ScheduleResponse(val status: String, val ids: List<String>)

@Serializable data class ErrorResponse(val error: String)

fun Application.configureRouting() {
    routing {
        staticResources("/", "static")

        authenticate("firebase-auth") {
            // Post Schedule
            post("/schedule") {
                try {
                    val user = call.principal<User>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val request = call.receive<SocialContentRequest>()
                    
                    val networksToPublish =
                        if (request.networks.isNotEmpty()) request.networks.split("|")
                        else listOf("linkedin")
                    
                    val scheduledIds = mutableListOf<String>()

                    for (network in networksToPublish) {
                        val parsedNetwork =
                            when (network) {
                                "twitter" -> SocialNetwork.TWITTER
                                else -> SocialNetwork.LINKEDIN
                            }

                        val parsedTime =
                            if (request.scheduledTime == "AUTOMATIC") {
                                val userSettings = UserSettingsRegistry.getSettingsForUser(user.userId)
                                val futureAutoScheduledPosts = DataStoreWrapper.getFutureAutoScheduledPosts(user.userId, parsedNetwork)
                                calculateOptimizedScheduleTime(parsedNetwork, userSettings, futureAutoScheduledPosts)
                            } else if (request.scheduledTime == "NOW") {
                                LocalDateTime.now(AppConfig.timeZone)
                            } else {
                                LocalDateTime.parse(request.scheduledTime)
                                    .atZone(AppConfig.timeZone)
                                    .toLocalDateTime()
                            }

                        val contentID = DataStoreWrapper.saveSocialContent(
                            userId = user.userId,
                            textContent = request.textContent,
                            urlContent = request.urlContent,
                            scheduledTime = parsedTime,
                            network = parsedNetwork,
                            tags = request.tags.split(" ")
                        )

                        if (request.scheduledTime == "NOW") {
                            publishingScope.launch {
                                try {
                                    val targetUrn =
                                        when (parsedNetwork) {
                                            SocialNetwork.TWITTER -> {
                                                TwitterConnector.publishToTwitterTimeline(
                                                    userId = user.userId,
                                                    textContent = request.textContent,
                                                    urlContent = request.urlContent,
                                                    tags = request.tags.split(" ")
                                                )
                                            }
                                            else -> {
                                                LinkedinConnector.publishToOrganizationTimeline(
                                                    userId = user.userId,
                                                    textContent = request.textContent,
                                                    urlContent = request.urlContent,
                                                    tags = request.tags.split(" ")
                                                )
                                            }
                                        }

                                    DataStoreWrapper.updateStatus(contentID, PostStatus.PUBLISHED, targetUrn)
                                } catch (e: Exception) {
                                    DataStoreWrapper.updateStatus(contentID, PostStatus.FAILED)
                                    val errorText = e.stackTraceToString()
                                    call.application.log.error("Error scheduling Post: $errorText")
                                }
                            }
                            DataStoreWrapper.updateStatus(contentID, PostStatus.PUBLISHING)
                        } else {
                            CloudTasks.createHttpTask(
                                projectId = AppConfig.projectId,
                                locationId = AppConfig.cloudLocationId,
                                queueId = AppConfig.cloudTasksQueueId,
                                url = "${AppConfig.baseUrl}/publish/${contentID}",
                                serviceAccountEmail = "${AppConfig.serviceAccount}",
                                scheduleTime = parsedTime.atZone(AppConfig.timeZone).toInstant()
                            )

                            val finalStatus = if (request.scheduledTime == "AUTOMATIC") PostStatus.AUTOSCHEDULED else PostStatus.SCHEDULED
                            DataStoreWrapper.updateStatus(contentID, finalStatus)
                        }

                        scheduledIds.add(contentID)
                    }

                    call.application.log.info("Scheduled Post: ${request.textContent} for networks: $networksToPublish")
                    call.respond(ScheduleResponse("success", scheduledIds))
                } catch (e: Exception) {
                    val errorText = e.stackTraceToString()
                    call.application.log.error("Error scheduling Post: $errorText")
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(errorText))
                }
            }
        }

        //
        // Rutas para Cloud Tasks
        authenticate("google-cloud-tasks") {
            // Solo accesible con el OIDC Token de Google
            post("/publish/{id}") {
                val postId = call.parameters["id"]
                if (postId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing post ID")
                    call.application.log.error("Bad request, missing post ID")
                    return@post
                }

                // Recuperar la entidad desde DataStore
                val datastore = DatastoreOptions.getDefaultInstance().service
                val key = datastore.newKeyFactory().setKind("SocialContent").newKey(postId)
                val entity = datastore.get(key)

                if (entity == null) {
                    call.respond(HttpStatusCode.NotFound, "Post no encontrado")
                    call.application.log.error("Post not found (ID: $postId)")
                    return@post
                }

                try {
                    val userId = entity.getString("userId")
                    val textContent = entity.getString("textContent")
                    val urlContent = if (entity.contains("urlContent")) entity.getString("urlContent") else null

                    // Recuperamos los tags si existiesen en Datastore
                    val tags =
                        if (entity.contains("tags")) {
                            entity.getList<com.google.cloud.datastore.Value<*>>("tags").map { it.get().toString() }
                        } else emptyList()

                    val network = if (entity.contains("network")) entity.getString("network") else "linkedin"
                    var targetUrn: String? = null
                    var tweetId: String? = null

                    if (network == "LINKEDIN") {
                        targetUrn = LinkedinConnector.publishToOrganizationTimeline(
                            userId = userId,
                            textContent = textContent,
                            urlContent = urlContent,
                            tags = tags
                        )
                    } else if (network == "TWITTER") {
                        tweetId = TwitterConnector.publishToTwitterTimeline(
                            userId = userId,
                            textContent = textContent,
                            urlContent = urlContent,
                            tags = tags
                        )
                    }

                    val updatedEntityBuilder = Entity.newBuilder(entity).set("status", PostStatus.PUBLISHED.name)

                    if (targetUrn != null)
                        updatedEntityBuilder.set("targetUrn", targetUrn)
                    else if (tweetId != null)
                        updatedEntityBuilder.set("targetUrn", tweetId)

                    datastore.put(updatedEntityBuilder.build())

                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.application.log.error("Failed to publish post (ID: $postId)", e)
                    val failedEntity = Entity.newBuilder(entity).set("status", PostStatus.FAILED.name).build()
                    datastore.put(failedEntity)
                    call.respond(HttpStatusCode.InternalServerError, "Error publishing post: ${e.message}")
                }
            }
        }
    }
}

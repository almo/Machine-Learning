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
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*

import com.google.cloud.datastore.*
import java.util.concurrent.ConcurrentHashMap

val publishingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

@Serializable
data class SweetSpotDTO(
    val start: String,
    val end: String
)

@Serializable
data class ScheduleSettingsDTO(
    val twitterLimit: Int = 5,
    val linkedinLimit: Int = 2,
    val sweetSpots: List<SweetSpotDTO> = listOf(
        SweetSpotDTO("07:30", "09:30"),
        SweetSpotDTO("12:30", "14:00"),
        SweetSpotDTO("17:30", "19:30"),
        SweetSpotDTO("21:00", "22:30")
    )
)

data class UserScheduleSettings(
    val dailyLimits: Map<SocialNetwork, Int>,
    val sweetSpots: List<Pair<LocalTime, LocalTime>>
)

fun ScheduleSettingsDTO.toDomain(): UserScheduleSettings {
    val spots = sweetSpots.mapNotNull { spot ->
        try {
            val start = LocalTime.parse(spot.start)
            val end = LocalTime.parse(spot.end)
            if (start.isBefore(end)) start to end else null
        } catch (e: Exception) {
            null
        }
    }
    return UserScheduleSettings(
        dailyLimits = mapOf(
            SocialNetwork.TWITTER to twitterLimit.coerceIn(1, 50),
            SocialNetwork.LINKEDIN to linkedinLimit.coerceIn(1, 50)
        ),
        sweetSpots = if (spots.isNotEmpty()) spots else UserSettingsRegistry.defaultSettings.sweetSpots
    )
}

fun UserScheduleSettings.toDTO(): ScheduleSettingsDTO {
    return ScheduleSettingsDTO(
        twitterLimit = dailyLimits[SocialNetwork.TWITTER] ?: 5,
        linkedinLimit = dailyLimits[SocialNetwork.LINKEDIN] ?: 2,
        sweetSpots = sweetSpots.map {
            SweetSpotDTO(
                start = it.first.toString(),
                end = it.second.toString()
            )
        }
    )
}

object UserSettingsRegistry {
    private val datastore by lazy { DatastoreOptions.getDefaultInstance().service }
    private const val KIND = "UserSettings"

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

    private val cache = ConcurrentHashMap<String, UserScheduleSettings>()

    fun getSettingsForUser(userId: String): UserScheduleSettings {
        return cache.computeIfAbsent(userId) { loadFromDatastore(userId) ?: defaultSettings }
    }

    fun saveSettingsForUser(userId: String, settings: UserScheduleSettings) {
        val key = datastore.newKeyFactory().setKind(KIND).newKey(userId)
        val spotsList = ListValue.newBuilder()
        settings.sweetSpots.forEach { (start, end) ->
            spotsList.addValue(StringValue.of("$start-$end"))
        }

        val entity = Entity.newBuilder(key)
            .set("userId", userId)
            .set("twitterLimit", (settings.dailyLimits[SocialNetwork.TWITTER] ?: 5).toLong())
            .set("linkedinLimit", (settings.dailyLimits[SocialNetwork.LINKEDIN] ?: 2).toLong())
            .set("sweetSpots", spotsList.build())
            .set("updatedAt", Timestamp.now())
            .build()

        datastore.put(entity)
        cache[userId] = settings
    }

    private fun loadFromDatastore(userId: String): UserScheduleSettings? {
        return try {
            val key = datastore.newKeyFactory().setKind(KIND).newKey(userId)
            val entity = datastore.get(key) ?: return null

            val twitterLimit = if (entity.contains("twitterLimit")) entity.getLong("twitterLimit").toInt() else 5
            val linkedinLimit = if (entity.contains("linkedinLimit")) entity.getLong("linkedinLimit").toInt() else 2

            val spots = if (entity.contains("sweetSpots")) {
                entity.getList<com.google.cloud.datastore.Value<*>>("sweetSpots").mapNotNull { value ->
                    try {
                        val str = value.get().toString()
                        val parts = str.split("-")
                        if (parts.size == 2) {
                            val start = LocalTime.parse(parts[0])
                            val end = LocalTime.parse(parts[1])
                            if (start.isBefore(end)) start to end else null
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            } else emptyList()

            UserScheduleSettings(
                dailyLimits = mapOf(
                    SocialNetwork.TWITTER to twitterLimit,
                    SocialNetwork.LINKEDIN to linkedinLimit
                ),
                sweetSpots = if (spots.isNotEmpty()) spots else defaultSettings.sweetSpots
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Calculates the optimal scheduling time for a social media post based on sweet spots, daily limits, 
 * and existing auto-scheduled posts.
 * 
 * @param network The target social network.
 * @param settings The schedule settings for the user (limits, sweet spots).
 * @param existingPosts A list of LocalDateTime representing all existing posts for this user and network from today onwards.
 * @return A LocalDateTime representing the calculated optimized schedule time.
 */
fun calculateOptimizedScheduleTime(
    network: SocialNetwork,
    settings: UserScheduleSettings,
    existingPosts: List<LocalDateTime>
): LocalDateTime {
    val now = LocalDateTime.now(AppConfig.timeZone)
    var currentDate = now.toLocalDate()

    val dailyLimit = settings.dailyLimits[network] ?: 2
    val spots = settings.sweetSpots

    while (true) {
        val postsOnDate = existingPosts.filter { it.toLocalDate() == currentDate }
        
        if (postsOnDate.size < dailyLimit) {
            val availableSpots = spots.mapNotNull { spot ->
                val spotStart = LocalDateTime.of(currentDate, spot.first)
                val spotEnd = LocalDateTime.of(currentDate, spot.second)
                
                // Skip if spot is already completely in the past
                if (!spotEnd.isAfter(now)) return@mapNotNull null
                
                // Check if the spot is already occupied by any existing post
                val isOccupied = postsOnDate.any { it >= spotStart && it <= spotEnd }
                
                if (!isOccupied) (spotStart to spotEnd) else null
            }

            if (availableSpots.isNotEmpty()) {
                val (spotStart, spotEnd) = availableSpots.random()
                // Sweet spot found! Calculate an effective start in case we are currently inside it.
                val effectiveStart = if (spotStart.isBefore(now)) now else spotStart
                val secondsBetween = ChronoUnit.SECONDS.between(effectiveStart, spotEnd)
                
                val randomSeconds = if (secondsBetween > 0) (0..secondsBetween).random() else 0L
                return effectiveStart.plusSeconds(randomSeconds)
            }
        }
        
        // Roll over to the next calendar day
        currentDate = currentDate.plusDays(1)
    }
}

@Serializable data class ScheduleResponse(val status: String, val ids: List<String>)

@Serializable data class ErrorResponse(val error: String)

@Serializable data class AIGenerateRequest(val url: String)

@Serializable data class AIGenerateResponse(
    val linkedinCompany: String,
    val linkedinCompanyUrl: String = "",
    val linkedinCompanyTags: String = "",
    val twitter: String,
    val twitterUrl: String = "",
    val twitterTags: String = "",
    val linkedinBump: String,
    val strategyRationale: String = ""
)

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
                                "linkedin" -> SocialNetwork.LINKEDIN
                                "twitter" -> SocialNetwork.TWITTER
                                else -> SocialNetwork.LINKEDIN
                            }
                        
                        val parseProfile = 
                           when (network) {
                                "linkedin" -> SocialProfile.COMPANY
                                "twitter" -> SocialProfile.PERSONAL 
                                else -> SocialProfile.PERSONAL
                           }

                        val parsedTime =
                            if (request.scheduledTime == "AUTOMATIC") {
                                val userSettings = UserSettingsRegistry.getSettingsForUser(user.userId)
                                val existingPosts = DataStoreWrapper.getPostsForScheduling(user.userId, parsedNetwork)
                                calculateOptimizedScheduleTime(parsedNetwork, userSettings, existingPosts)
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
                            profile = parseProfile,
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
                            val taskName = CloudTasks.createHttpTask(
                                projectId = AppConfig.projectId,
                                locationId = AppConfig.cloudLocationId,
                                queueId = AppConfig.cloudTasksQueueId,
                                url = "${AppConfig.baseUrl}/publish/${contentID}",
                                serviceAccountEmail = "${AppConfig.serviceAccount}",
                                scheduleTime = parsedTime.atZone(AppConfig.timeZone).toInstant()
                            )

                            val finalStatus = if (request.scheduledTime == "AUTOMATIC") PostStatus.AUTOSCHEDULED else PostStatus.SCHEDULED
                            DataStoreWrapper.updateStatus(contentID, finalStatus, cloudTaskName = taskName)
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

            get("/api/settings/schedule") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    val settings = UserSettingsRegistry.getSettingsForUser(user.userId)
                    call.respond(settings.toDTO())
                } catch (e: Exception) {
                    call.application.log.error("Error fetching schedule settings for user ${user.userId}", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch settings"))
                }
            }

            put("/api/settings/schedule") {
                val user = call.principal<User>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                try {
                    val dto = call.receive<ScheduleSettingsDTO>()
                    val domain = dto.toDomain()
                    UserSettingsRegistry.saveSettingsForUser(user.userId, domain)
                    call.respond(HttpStatusCode.OK, domain.toDTO())
                } catch (e: Exception) {
                    call.application.log.error("Error saving schedule settings for user ${user.userId}", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid schedule settings payload"))
                }
            }
        }

        //
        // Rutas para Cloud Tasks y publicación manual
        authenticate("google-cloud-tasks", "firebase-auth") {
            // Accesible con el OIDC Token de Google o Firebase Auth
            post("/publish/{id}") {
                val postId = call.parameters["id"]
                if (postId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing post ID")
                    call.application.log.error("Bad request, missing post ID")
                    return@post
                }

                // Recuperar la entidad desde DataStore
                val datastore = DatastoreOptions.getDefaultInstance().service
                val keyFactory = datastore.newKeyFactory().setKind("SocialContent")
                val key = postId.toLongOrNull()?.let { keyFactory.newKey(it) } ?: keyFactory.newKey(postId)
                val entity = datastore.get(key)

                if (entity == null) {
                    call.respond(HttpStatusCode.NotFound, "Post no encontrado")
                    call.application.log.error("Post not found (ID: $postId)")
                    return@post
                }

                val currentStatus = if (entity.contains("status")) entity.getString("status") else ""

                if (currentStatus == PostStatus.PUBLISHED.name) {
                    call.application.log.info("Post already published (ID: $postId)")
                    call.respond(HttpStatusCode.OK, "Post already published")
                    return@post
                }

                if (currentStatus == PostStatus.DELETED.name || currentStatus == PostStatus.FAILED.name) {
                    call.application.log.warn("Post $postId is in status '$currentStatus'; skipping publication.")
                    call.respond(HttpStatusCode.OK, "Post is in status '$currentStatus', skipping publication")
                    return@post
                }

                if (currentStatus != PostStatus.SCHEDULED.name && currentStatus != PostStatus.AUTOSCHEDULED.name) {
                    call.application.log.warn("Post $postId is not in scheduled state (status='$currentStatus'); skipping.")
                    call.respond(HttpStatusCode.OK, "Post is not in scheduled state")
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
                    
                    val updatedEntityBuilder = Entity.newBuilder(entity).set("status", PostStatus.PUBLISHED.name)
                    
                    if (network == "LINKEDIN") {
                        targetUrn = LinkedinConnector.publishToOrganizationTimeline(
                            userId = userId,
                            textContent = textContent,
                            urlContent = urlContent,
                            tags = tags
                        )
                        
                        updatedEntityBuilder.set("profile",SocialProfile.COMPANY.name)

                    } else if (network == "TWITTER") {
                        tweetId = TwitterConnector.publishToTwitterTimeline(
                            userId = userId,
                            textContent = textContent,
                            urlContent = urlContent,
                            tags = tags
                        )

                        updatedEntityBuilder.set("profile",SocialProfile.PERSONAL.name)
                        
                    }

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

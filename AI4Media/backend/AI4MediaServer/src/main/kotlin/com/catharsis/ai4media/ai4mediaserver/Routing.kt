package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.content.*
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.LocalDateTime
import java.time.ZoneId
import com.google.cloud.Timestamp
import java.util.Date
import java.time.ZoneOffset

fun Application.configureRouting() {
    routing {
        staticResources("/", "static")

        authenticate("firebase-auth") {
            // Post Schedule
            post("/schedule") {
                try {
                    val user =
                            call.principal<User>()
                                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val request = call.receive<SocialContentRequest>()

                    val parsedTime =
                            if (request.scheduledTime == "AUTOMATIC") {
                                LocalDateTime.now(AppConfig.timeZone).plusMinutes(15) 
                            } else {
                                LocalDateTime.parse(request.scheduledTime).atZone(AppConfig.timeZone).toLocalDateTime()
                            }

                    val content =
                            SocialContent(
                                    userId = user.userId,
                                    textContent = request.textContent,
                                    targetUrn = request.urlContent,
                                    scheduledTime = parsedTime,
                                    createdTime = LocalDateTime.now(AppConfig.timeZone)
                            )

                    val datastore = DatastoreOptions.getDefaultInstance().service
                    val key =
                            datastore
                                    .newKeyFactory()
                                    .setKind("SocialContent")
                                    .newKey(content.id.toString())
                    val entity =
                            Entity.newBuilder(key)
                                    .set("userId", content.userId)
                                    .set("textContent", content.textContent)
                                    .set("targetUrn", content.targetUrn)
                                    .set("scheduledTime", Timestamp.of(java.sql.Timestamp.valueOf(content.scheduledTime)))
                                    .set("createdTime", Timestamp.of(java.sql.Timestamp.valueOf(content.createdTime)))
                                    .set("status", content.status.name)
                                    .build()

                    datastore.put(entity)

                    CloudTasks.createHttpTask(
                            projectId = AppConfig.projectId,
                            locationId = AppConfig.cloudLocationId,
                            queueId = AppConfig.cloudTasksQueueId,
                            url = "${AppConfig.baseUrl}/publish/${content.id}",
                            serviceAccountEmail = "${AppConfig.serviceAccount}",
                            scheduleTime = content.scheduledTime.atZone(AppConfig.timeZone).toInstant()
                    )

                    call.application.log.info("Scheduled Post: ${content.textContent}")
                    call.respond(mapOf("status" to "success", "id" to content.id.toString()))
                } catch (e: Exception) {
                    val errorText = e.stackTraceToString()
                    call.application.log.error("Error scheduling Post: $errorText")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to errorText))
                }
            }

            // --- Secure OAuth Initialization ---
            // The frontend calls these endpoints with the Firebase Bearer token.
            // The server sets the session cookie and returns the URL to navigate to.

            get("/api/auth/init-twitter") {
                val user =
                        call.principal<User>()
                                ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.sessions.set(AI4MediaSession(user.userId))
                call.respond(mapOf("url" to "/auth/twitter/authorize"))
            }

            get("/api/auth/init-linkedin") {
                val user =
                        call.principal<User>()
                                ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.sessions.set(AI4MediaSession(user.userId))
                call.respond(mapOf("url" to "/auth/linkedin/authorize"))
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

                call.application.log.info("Post published (ID: $postId)")
                
                call.respond(HttpStatusCode.OK)
            }
        }
        // --- OAuth Handlers (Browser Redirects) ---

        // Note: The /auth routes are triggered by the browser navigation after the init call.

        authenticate("auth-twitter") {
            get("/auth/twitter/authorize") { /* Redirects to Twitter */}
            get("/auth/twitter/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                val session = call.sessions.get<AI4MediaSession>()

                if (principal != null && session != null) {

                    TokenService.saveToken(
                            userId = session.userId,
                            provider = "twitter",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )
                    call.sessions.clear<AI4MediaSession>()

                    call.respondRedirect("/dashboard.html?success=true")
                } else {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            "Expired Session or Invalid Credentials"
                    )
                }
            }
        }

        authenticate("auth-linkedin") {
            get("/auth/linkedin/authorize") { /* Redirects to LinkedIn */}
            get("/auth/linkedin/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                val session = call.sessions.get<AI4MediaSession>()

                if (principal != null && session != null) {

                    TokenService.saveToken(
                            userId = session.userId,
                            provider = "linkedin",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )

                    call.sessions.clear<AI4MediaSession>()

                    call.respondRedirect("/dashboard.html?success=true")
                } else {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            "Expired Session or Invalid Credentials"
                    )
                }
            }
        }
    }
}

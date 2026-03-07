package com.catharsis.ai4media.ai4mediaserver

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.Instant

fun Application.configureRouting() {
    routing {
        staticResources("/", "static")

        authenticate("firebase-auth") {
            // Post Schedule
            get("/api/schedule_post") {
                try {
                    val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                    CloudTasks.createHttpTask(
                            call.application.attributes[cloudProjectId],
                            call.application.attributes[cloudLocationId],
                            call.application.attributes[cloudTasksQueueId],
                            "http://www.google.com?$user",
                            Instant.now().plusSeconds(10000)
                    )

                    call.application.log.info("Scheduled Post")
                    call.respondText("Scheduled Post", status = HttpStatusCode.OK)
                } catch (e: Exception) {
                    val errorText = e.stackTraceToString()
                    call.application.log.error("Error scheduling Post: $errorText")
                    call.respondText(errorText, status = HttpStatusCode.InternalServerError)
                }
            }

            // --- Secure OAuth Initialization ---
            // The frontend calls these endpoints with the Firebase Bearer token.
            // The server sets the session cookie and returns the URL to navigate to.
            
            get("/api/auth/init-twitter") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.sessions.set(AI4MediaSession(user.userId))
                call.respond(mapOf("url" to "/auth/twitter/authorize"))
            }

            get("/api/auth/init-linkedin") {
                val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.sessions.set(AI4MediaSession(user.userId))
                call.respond(mapOf("url" to "/auth/linkedin/authorize"))
            }
        }
        // --- OAuth Handlers (Browser Redirects) ---

        // Note: The /auth routes are triggered by the browser navigation after the init call.

        authenticate("auth-twitter") {
            get("/auth/twitter/authorize") { /* Redirects to Twitter */ }
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
            get("/auth/linkedin/authorize") { /* Redirects to LinkedIn */ }
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

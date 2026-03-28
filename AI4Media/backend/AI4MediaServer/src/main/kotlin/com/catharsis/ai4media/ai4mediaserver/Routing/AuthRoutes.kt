package com.catharsis.ai4media.ai4mediaserver

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureAuthRouting() {
    routing {
        authenticate("firebase-auth") {
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

            get("/api/auth/status") {
                try {
                    val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    
                    val twitterToken = TokenService.getValidToken(user.userId, "twitter", AppConfig.twitterClientId, AppConfig.twitterClientSecret)
                    val linkedinToken = TokenService.getValidToken(user.userId, "linkedin", AppConfig.linkedinClientId, AppConfig.linkedinClientSecret)
                    
                    call.respond(
                        mapOf(
                            "twitter" to (twitterToken != null),
                            "linkedin" to (linkedinToken != null)
                        )
                    )
                } catch (e: Exception) {
                    call.application.log.error("Error fetching auth status", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.stackTraceToString()))
                }
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
                    TokenService.saveToken(session.userId, "twitter", principal.accessToken, principal.refreshToken, principal.expiresIn.toInt())
                    call.sessions.clear<AI4MediaSession>()
                    call.respondRedirect("/dashboard.html?success=true")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Expired Session or Invalid Credentials")
                }
            }
        }

        authenticate("auth-linkedin") {
            get("/auth/linkedin/authorize") { /* Redirects to LinkedIn */ }
            get("/auth/linkedin/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                val session = call.sessions.get<AI4MediaSession>()

                if (principal != null && session != null) {
                    TokenService.saveToken(session.userId, "linkedin", principal.accessToken, principal.refreshToken, principal.expiresIn.toInt())
                    call.sessions.clear<AI4MediaSession>()
                    call.respondRedirect("/dashboard.html?success=true")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Expired Session or Invalid Credentials")
                }
            }
        }
    }
}
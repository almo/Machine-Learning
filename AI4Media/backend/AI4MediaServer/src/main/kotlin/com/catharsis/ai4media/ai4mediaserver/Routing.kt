package com.catharsis.ai4media.ai4mediaserver

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.configureRouting() {
    routing {
        staticResources("/", "static")

        authenticate("firebase-auth") {
            // Post Schedule
            get("/api/schedule_post") {
                var respondText = "Scheduled Post"

                try {
                    CloudTasks.createHttpTask(
                            call.application.attributes[cloudProjectId],
                            call.application.attributes[cloudLocationId],
                            call.application.attributes[cloudTasksQueueId],
                            "http://www.google.com",
                            Instant.now().plusSeconds(10000)
                    )
                } catch (e: Exception) {
                    respondText = e.toString()
                }

                call.respondText(respondText)
            }
        }
        // --- OAuth Handlers (Browser Redirects) ---

        // Note: These routes initiate the OAuth dance.
        // We capture the userId from query params to pass into 'state'.
        authenticate("auth-twitter") {
            get("/login/twitter") {
                // Ktor automatically redirects to Twitter
            }

            get("/auth/twitter/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                // In a real app, you must decode the 'state' parameter to recover the userId
                // For this example, we assume we can identify the user or the session is active.

                if (principal != null) {
                    // Ideally, extract userId from 'state' or session.
                    // Hardcoding a demo ID or assuming session for this snippet:
                    val userId = "demo-user-id"

                    TokenService.saveToken(
                            userId = userId,
                            provider = "twitter",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )
                    call.respondText("Twitter Linked Successfully!")
                }
            }
        }

        authenticate("auth-linkedin") {
            get("/login/linkedin") { /* Redirects */}

            get("/auth/linkedin/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                if (principal != null) {
                    val userId = "demo-user-id" // Retrieve from state/session

                    TokenService.saveToken(
                            userId = userId,
                            provider = "linkedin",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )
                    call.respondText("LinkedIn Linked Successfully!")
                }
            }
        }
    }
}

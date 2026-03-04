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
                    CloudTasks.createHttpTask(
                            call.application.attributes[cloudProjectId],
                            call.application.attributes[cloudLocationId],
                            call.application.attributes[cloudTasksQueueId],
                            "http://www.google.com",
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
        }
        // --- OAuth Handlers (Browser Redirects) ---

        // Note: These routes initiate the OAuth dance.
        // We capture the userId from query params to pass into 'state'.
        get("/login/twitter") {
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing userId")
           // call.sessions.set(OAuthSession(userId))
            call.respondRedirect("/auth/twitter/authorize")
        }

        authenticate("auth-twitter") {
            get("/auth/twitter/authorize") { /* Redirects to Twitter */ }
            get("/auth/twitter/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

               // val session = call.sessions.get<OAuthSession>()

               val session = "session.userId;"

                if (principal != null && session != null) {

                    TokenService.saveToken(
                            userId = "session.userId",
                            provider = "twitter",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )
                   // call.sessions.clear<OAuthSession>()

                    call.respondRedirect("/dashboard.html?success=true")
                } else {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            "Expired Session or Invalid Credentials"
                    )
                }
            }
        }

        get("/login/linkedin") {
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing userId")
            //call.sessions.set(OAuthSession(userId))
            call.respondRedirect("/auth/linkedin/authorize")
        }

        authenticate("auth-linkedin") {
            get("/auth/linkedin/authorize") { /* Redirects to LinkedIn */ }
            get("/auth/linkedin/callback") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

               // val session = call.sessions.get<OAuthSession>()
                val session = "session.userId;"

                if (principal != null && session != null) {

                    TokenService.saveToken(
                            userId = "session.userId",
                            provider = "linkedin",
                            accessToken = principal.accessToken,
                            refreshToken = principal.refreshToken,
                            expiresIn = principal.expiresIn.toInt()
                    )

                   // call.sessions.clear<OAuthSession>()

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

package com.catharsis.ai4media.ai4mediaserver

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class User(val userId: String, val email: String, val tenantId: String?)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    if (FirebaseApp.getApps().isEmpty()) {
        try {
            val options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId("meta-gear-464720-g3")
                            .build()
            FirebaseApp.initializeApp(options)
            log.info("Firebase Initialized")
        } catch (e: Exception) {
            log.error("Firebase Init Failed", e)
        }
    }

    install(ContentNegotiation) { json() }

    install(Authentication) {
        // "bearer" is a built-in Ktor auth scheme for Token headers
        bearer("firebase-auth") {
            realm = "AI4Media Access"
            authenticate { tokenCredential ->
                try {
                    val token = tokenCredential.token
                    // Verify with Firebase
                    val decodedToken: FirebaseToken =
                            FirebaseAuth.getInstance().verifyIdToken(token)

                    // Extract Tenant ID
                    val tenantId = decodedToken.claims["tenantProjectId"] as? String

                    // Return the Principal (Success) or null (Failure)
                    User(userId = decodedToken.uid, email = decodedToken.email, tenantId = tenantId)
                } catch (e: Exception) {
                    // Log the error and return null to reject the request
                    this@module.log.error("Firebase Auth Failed", e)
                }
            }
        }
    }

    routing {
        staticResources("/", "static")

        authenticate("firebase-auth") {
            get("/api/schedule_post") { 
                val user = call.principal<User>()
                call.respondText("Hello ${user?.userId}, Welcome to the AI4Media Server!") 
            }
        }
    }
}

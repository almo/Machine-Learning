package com.catharsis.ai4media.ai4mediaserver

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.sessions.*

data class User(val userId: String, val email: String, val tenantId: String?)


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    // Initialize Firebase: 
    // Retrieve Google Cloud Project ID from environment variables
    val projectId = System.getenv("GOOGLE_CLOUD_PROJECT") 
        ?: throw IllegalStateException("GOOGLE_CLOUD_PROJECT env var not set")

    // Determine the base URL for callbacks, defaulting to localhost for development
    val baseUrl = System.getenv("APPENGINE_BASE_URL")?.removeSuffix("/") ?: "http://localhost:8080"

    // Initialize Firebase App if not already initialized
    if (FirebaseApp.getApps().isEmpty()) {
        try {
            val options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(projectId)
                            .build()
            FirebaseApp.initializeApp(options)
            log.info("Firebase Initialized")
        } catch (e: Exception) {
            log.error("Firebase Init Failed", e)
        }
    }

    // Retrieve client IDs and secrets for Twitter and LinkedIn from Google Secret Manager
    val twitterClientId = SecretManager.getSecret(projectId, "TWITTER_CLIENT_ID")
    val twitterClientSecret = SecretManager.getSecret(projectId, "TWITTER_CLIENT_SECRET")
    val linkedinClientId = SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_ID")
    val linkedinClientSecret = SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_SECRET")

    // Install ContentNegotiation feature for automatic JSON serialization/deserialization
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

        // --- OAuth Schemes ---
        // Twitter OAuth 2.0
        oauth("auth-twitter") {
            urlProvider = { "$baseUrl/auth/twitter/callback" } // Callback URL for Twitter OAuth
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "twitter",
                    authorizeUrl = "https://twitter.com/i/oauth2/authorize",
                    accessTokenUrl = "https://api.twitter.com/2/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = twitterClientId,
                    clientSecret = twitterClientSecret,
                    defaultScopes = listOf("tweet.read", "tweet.write", "users.read", "offline.access") // offline.access needed for refresh
                )
            }
            client = HttpClient(CIO)
        }

        // LinkedIn OAuth 2.0
        oauth("auth-linkedin") {
            urlProvider = { "$baseUrl/auth/linkedin/callback" } // Callback URL for LinkedIn OAuth
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "linkedin",
                    authorizeUrl = "https://www.linkedin.com/oauth/v2/authorization",
                    accessTokenUrl = "https://www.linkedin.com/oauth/v2/accessToken",
                    requestMethod = HttpMethod.Post,
                    clientId = linkedinClientId,
                    clientSecret = linkedinClientSecret,
                    defaultScopes = listOf("r_liteprofile", "r_emailaddress", "w_member_social")
                )
            }
            client = HttpClient(CIO)
        }
    }

    // Configure application routing
    this.configureRouting()
}
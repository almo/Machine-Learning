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
import io.ktor.util.*
import java.util.Base64
import kotlinx.serialization.Serializable

@Serializable data class User(val userId: String, val email: String, val tenantId: String?)

@Serializable data class AI4MediaSession(val userId: String)

val cloudProjectId = AttributeKey<String>("CLOUD_PROJECT_ID")
val cloudLocationId = AttributeKey<String>("CLOUD_LOCATION_ID")
val cloudTasksQueueId = AttributeKey<String>("CLOUD_TASKS_QUEUE_ID")

val twitterClientId = AttributeKey<String>("TWITTER_CLIENT_ID")
val twitterClientSecret = AttributeKey<String>("TWITTER_CLIENT_SECRET")
val linkedinClientId = AttributeKey<String>("LINKEDIN_CLIENT_ID")
val linkedinClientSecret = AttributeKey<String>("LINKEDIN_CLIENT_SECRET")

val sessionKey = AttributeKey<String>("OAUTH_SESSION_SECRET_KEY")
val secretEncryptKey = AttributeKey<String>("SECRET_ENCRYPT_KEY")

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    // Initialize Firebase:
    // Retrieve Google Cloud Project ID from environment variables
    val projectId =
            System.getenv("GOOGLE_CLOUD_PROJECT")
                    ?: throw IllegalStateException("GOOGLE_CLOUD_PROJECT env var not set")
    log.info("Env: Google Cloud Project ID set...")

    // Determine the base URL for callbacks, defaulting to localhost for development
    val baseUrl =
            System.getenv("APPENGINE_BASE_URL")?.removeSuffix("/")
                    ?: throw IllegalStateException("APPENGINE_BASE_URL env var not set")
    log.info("Env: Base URL set...")

    // Initialize Firebase App if not already initialized
    if (FirebaseApp.getApps().isEmpty()) {
        try {
            val options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(projectId)
                            .build()
            FirebaseApp.initializeApp(options)
        } catch (e: Exception) {
            log.error("Firebase Init Failed", e)
            throw IllegalStateException("Firebase Init Failed")
        }
    }
    log.info("Firebase initialized...")

    // Setting up cloud conf
    attributes.put(cloudProjectId, projectId)
    attributes.put(cloudLocationId, SecretManager.getSecret(projectId, "CLOUD_LOCATION_ID"))
    attributes.put(cloudTasksQueueId, SecretManager.getSecret(projectId, "CLOUD_TASKS_QUEUE_ID"))
    log.info("Secret Manager: Google Cloud configuration set...")

    // Retrieve client IDs and secrets for Twitter and LinkedIn from Google Secret Manager, setting
    // up attributes
    attributes.put(twitterClientId, SecretManager.getSecret(projectId, "TWITTER_CLIENT_ID"))
    attributes.put(twitterClientSecret, SecretManager.getSecret(projectId, "TWITTER_CLIENT_SECRET"))
    log.info("Secret Manager: Twitter oauth configuration set...")

    attributes.put(linkedinClientId, SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_ID"))
    attributes.put(
            linkedinClientSecret,
            SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_SECRET")
    )
    log.info("Secret Manager: LinkedIn oauth configuration set...")

    // Retrieve Signature and Encryption Keys from Google Secret Manager, setting up attribute
    val sessionSecretString = SecretManager.getSecret(projectId, "OAUTH_SESSION_SECRET_KEY")
    attributes.put(sessionKey, sessionSecretString)
    log.info("Secret Manager: AI4Media session signature secret set...")

    val sessionEncryptKey = SecretManager.getSecret(projectId, "SECRET_ENCRYPT_KEY")
    attributes.put(secretEncryptKey, sessionEncryptKey)
    log.info("Secret Manager: AI4Media session encryptioin secret set...")

    // Install ContentNegotiation feature for automatic JSON serialization/deserialization
    install(ContentNegotiation) { json() }
    log.info("Content negotiation set...")

    // Install Sessions feature for managing user sessions
    install(Sessions) {
        cookie<AI4MediaSession>("AI4MEDIA_SESSION") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = true
            cookie.maxAgeInSeconds = 180

            transform(
                    SessionTransportTransformerEncrypt(
                            Base64.getDecoder().decode(sessionEncryptKey),
                            Base64.getDecoder().decode(sessionSecretString),
                            encryptAlgorithm = "AES", 
                            signAlgorithm = "HmacSHA256"
                    )
            )
        }
    }
    log.info("Session management set...")

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
                        clientId = this@module.attributes[twitterClientId],
                        clientSecret = this@module.attributes[twitterClientSecret],
                        defaultScopes =
                                listOf(
                                        "tweet.read",
                                        "tweet.write",
                                        "users.read",
                                        "offline.access"
                                ) // offline.access needed for refresh
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
                        clientId = this@module.attributes[linkedinClientId],
                        clientSecret = this@module.attributes[linkedinClientSecret],
                        defaultScopes = listOf("openid", "profile", "email", "w_member_social")
                )
            }
            client = HttpClient(CIO)
        }
    }
    log.info("Authentication set...")

    // Configure application routing
    this.configureRouting()
}

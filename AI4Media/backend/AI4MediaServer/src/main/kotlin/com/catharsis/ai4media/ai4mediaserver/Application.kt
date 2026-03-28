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
import java.security.SecureRandom
import java.util.Base64
import kotlin.system.exitProcess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class User(val userId: String, val email: String)

@Serializable
data class AI4MediaSession(
        val userId: String,
        val codeVerifier: String = oAuthPKCE.generateCodeVerifier()
)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    // Initialize Firebase App if not already initialized
    try {
        if (FirebaseApp.getApps().isEmpty()) {
            val options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(AppConfig.projectId)
                            .build()
            FirebaseApp.initializeApp(options)
        }
    } catch (e: Exception) {
        log.error("Firebase Init Failed", e)
        throw IllegalStateException("Firebase Init Failed")
    }
    log.info("Firebase initialized...")

    // Install ContentNegotiation feature for automatic JSON serialization/deserialization
    try {
        install(ContentNegotiation) {
            json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
            )
        }
        log.info("Content negotiation set...")
    } catch (e: Exception) {
        log.error("Critical error initializing the AI4Media server: ${e.message}")
        exitProcess(1)
    }

    // Install Sessions feature for managing user sessions
    install(Sessions) {
        cookie<AI4MediaSession>("AI4MEDIA_SESSION") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = true
            cookie.maxAgeInSeconds = 180

            transform(
                    SessionTransportTransformerEncrypt(
                            Base64.getDecoder().decode(AppConfig.sessionEncryptKey),
                            Base64.getDecoder().decode(AppConfig.sessionSecretString),
                            encryptAlgorithm = "AES",
                            signAlgorithm = "HmacSHA256",
                            ivGenerator = { SecureRandom().generateSeed(16) }
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

                    // Return the Principal (Success) or null (Failure)
                    User(userId = decodedToken.uid, email = decodedToken.email)
                } catch (e: Exception) {
                    // Log the error and return null to reject the request
                    this@module.log.error("Firebase Auth Failed", e)
                    null
                }
            }
        }

        // Cloud Task
        bearer("google-cloud-tasks") {
            realm = "AI4Media Access - Google Cloud Tasks"
            authenticate { credential ->
                if (TokenVerifier.verify(credential.token)) {
                    User(userId = "google-cloud-service-account", email = AppConfig.serviceAccount)
                } else {
                    null
                }
            }
        }

        // --- OAuth Schemes ---
        // Twitter OAuth 2.0
        oauth("auth-twitter") {
            urlProvider = {
                "${AppConfig.baseUrl}/auth/twitter/callback"
            } // Callback URL for Twitter OAuth
            providerLookup = {
                var session = sessions.get<AI4MediaSession>()

                if (session == null) {
                    session = AI4MediaSession("unknown_user") 
                    sessions.set(session)
                }

                val codeVerifier = session.codeVerifier 
                val codeChallenge = oAuthPKCE.generateCodeChallenge(codeVerifier)

                OAuthServerSettings.OAuth2ServerSettings(
                        name = "twitter",
                        authorizeUrl = "https://twitter.com/i/oauth2/authorize",
                        accessTokenUrl = "https://api.twitter.com/2/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = AppConfig.twitterClientId,
                        clientSecret = AppConfig.twitterClientSecret,
                        accessTokenRequiresBasicAuth = true,
                        defaultScopes =
                                listOf("tweet.read", "tweet.write", "users.read", "offline.access"),
                        extraAuthParameters =
                                listOf(
                                        "code_challenge" to codeChallenge,
                                        "code_challenge_method" to "S256"
                                ),
                        accessTokenInterceptor = {
                            url.parameters.append("code_verifier", codeVerifier)
                        }
                )
            }
            client = HttpClient(CIO) { install(io.ktor.client.plugins.auth.Auth) }
        }

        // LinkedIn OAuth 2.0
        oauth("auth-linkedin") {
            urlProvider = {
                "${AppConfig.baseUrl}/auth/linkedin/callback"
            } // Callback URL for LinkedIn OAuth
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                        name = "linkedin",
                        authorizeUrl = "https://www.linkedin.com/oauth/v2/authorization",
                        accessTokenUrl = "https://www.linkedin.com/oauth/v2/accessToken",
                        requestMethod = HttpMethod.Post,
                        clientId = AppConfig.linkedinClientId,
                        clientSecret = AppConfig.linkedinClientSecret,
                        defaultScopes =
                                listOf(
                                        "openid",
                                        "profile",
                                        "email",
                                        "w_member_social",
                                        "w_organization_social",
                                        "r_organization_social",
                                        "rw_organization_admin"
                                )
                )
            }
            client = HttpClient(CIO) { install(io.ktor.client.plugins.auth.Auth) }
        }
    }
    log.info("Authentication set...")

    // Configure application routing
    this.configureRouting()
    this.configureSourceRouting()
    this.configureAuthRouting()
    this.configureNewsRouting()
}

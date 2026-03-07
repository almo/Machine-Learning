package com.catharsis.ai4media.ai4mediaserver

import java.util.Base64


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
import kotlinx.serialization.Serializable
import kotlin.system.exitProcess

@Serializable data class User(val userId: String, val email: String, val tenantId: String?)

@Serializable data class AI4MediaSession(val userId: String)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    try {
        install(ContentNegotiation) { json() }
        log.info("Content negotiation set...")
    }catch (e: Exception){
        log.error("Critical error initializing the AI4Media server: ${e.message}")
        exitProcess(1)
    }

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
            urlProvider = { "$AppConfig.baseUrl/auth/twitter/callback" } // Callback URL for Twitter OAuth
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                        name = "twitter",
                        authorizeUrl = "https://twitter.com/i/oauth2/authorize",
                        accessTokenUrl = "https://api.twitter.com/2/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = AppConfig.twitterClientId,
                        clientSecret = AppConfig.twitterClientSecret,
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
            urlProvider = { "$AppConfig.baseUrl/auth/linkedin/callback" } // Callback URL for LinkedIn OAuth
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                        name = "linkedin",
                        authorizeUrl = "https://www.linkedin.com/oauth/v2/authorization",
                        accessTokenUrl = "https://www.linkedin.com/oauth/v2/accessToken",
                        requestMethod = HttpMethod.Post,
                        clientId = AppConfig.linkedinClientId,
                        clientSecret = AppConfig.linkedinClientSecret,
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

package com.catharsis.ai4media.ai4mediaserver

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.slf4j.LoggerFactory

/**
 * Utility object responsible for verifying Google OpenID Connect (OIDC) tokens.
 * This is typically used to authenticate incoming requests from Google services like Cloud Tasks.
 */
object TokenVerifier {
    private val logger = LoggerFactory.getLogger(TokenVerifier::class.java)
    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    private val verifier =
            GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(listOf("${AppConfig.baseUrl}"))
                    .setIssuer("https://accounts.google.com")
                    .build()

    /**
     * Verifies the authenticity and validity of the provided OIDC token.
     *
     * @param token The raw JWT token string to verify.
     * @return `true` if the token is valid, signed by Google, matches the audience, and was issued for the designated service account; `false` otherwise.
     */
    fun verify(token: String): Boolean {
        return try {
            val idToken = verifier.verify(token)
            if (idToken == null) {
                logger.warn("OIDC Token verification failed: Invalid signature or claims")
                return false
            }

            val payload = idToken.payload
            val email = payload.email
            val emailVerified = payload.emailVerified

            if (email.isNullOrBlank() || emailVerified != true) {
                logger.warn("OIDC Token verification failed: Missing or unverified email claim")
                return false
            }

            if (email != AppConfig.serviceAccount) {
                logger.warn("OIDC Token verification failed: Caller email '$email' does not match expected service account '${AppConfig.serviceAccount}'")
                return false
            }

            true
        } catch (e: Exception) {
            // Es vital loguear esto para debuguear problemas de red o expiración
            logger.error("Error verifying OIDC Token: ${e.message}")
            false
        }
    }
}

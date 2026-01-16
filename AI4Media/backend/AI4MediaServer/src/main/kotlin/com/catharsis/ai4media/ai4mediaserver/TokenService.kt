package com.catharsis.ai4media.ai4mediaserver

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

// --- Data Models for Tokens ---
@Serializable
data class OAuthTokenData(
    val accessToken: String,
    val refreshToken: String?,
    val provider: String, // "twitter" or "linkedin"
    val expiresAt: Long // Epoch Second
)

// Represents the response from Twitter/LinkedIn Token endpoints
@Serializable
data class TokenRefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Int
)

// --- Token Service (Handles Refreshing) ---
object TokenService {
    // In production, use a Database (Firestore/Postgres). This is In-Memory for demo.
    private val tokenStore = mutableMapOf<String, OAuthTokenData>() // Key: "userId-provider"

    // Internal HTTP Client for backend-to-backend refresh calls
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun saveToken(userId: String, provider: String, accessToken: String, refreshToken: String?, expiresIn: Int) {
        val expiresAt = Instant.now().plusSeconds(expiresIn.toLong()).epochSecond
        tokenStore["$userId-$provider"] = OAuthTokenData(accessToken, refreshToken, provider, expiresAt)
    }

    /**
     * Gets a valid Access Token. If expired, it refreshes it automatically.
     */
    suspend fun getValidToken(userId: String, provider: String, clientId: String, clientSecret: String): String? {
        val key = "$userId-$provider"
        val data = tokenStore[key] ?: return null

        // Check if expired (buffer of 60 seconds)
        if (Instant.now().epochSecond > data.expiresAt - 60) {
            return refreshToken(userId, data, clientId, clientSecret)
        }
        return data.accessToken
    }

    private suspend fun refreshToken(userId: String, oldToken: OAuthTokenData, clientId: String, clientSecret: String): String? {
        if (oldToken.refreshToken == null) return null // Cannot refresh

        val url = if (oldToken.provider == "twitter") "https://api.twitter.com/2/oauth2/token" else "https://www.linkedin.com/oauth/v2/accessToken"
        
        try {
            val response: TokenRefreshResponse = httpClient.submitForm(
                url = url,
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", oldToken.refreshToken)
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                }
            ).body()

            // Update Store
            saveToken(userId, oldToken.provider, response.accessToken, response.refreshToken ?: oldToken.refreshToken, response.expiresIn)
            return response.accessToken
        } catch (e: Exception) {
            println("Failed to refresh token: ${e.message}")
            return null
        }
    }
}
package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.EntityValue
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.IncompleteKey
import com.google.cloud.datastore.Key
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    private val datastore by lazy {
        val projectId = System.getenv("GOOGLE_CLOUD_PROJECT")
        if (projectId != null) {
            DatastoreOptions.newBuilder().setProjectId(projectId).build().service
        } else {
            DatastoreOptions.getDefaultInstance().service
        }
    }
    // ToDo -- configurations
    private const val KIND = "UserTokens"

    // Internal HTTP Client for backend-to-backend refresh calls
    private val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }

    fun saveToken(
            userId: String,
            provider: String,
            accessToken: String,
            refreshToken: String?,
            expiresIn: Int
    ) {
        println("Saving token for userId: $userId, provider: $provider")
        val keyFactory = datastore.newKeyFactory().setKind(KIND)
        val key = keyFactory.newKey(userId)

        val transaction = datastore.newTransaction()
        try {
            val existingEntity = transaction.get(key)
            val builder = if (existingEntity != null) {
                Entity.newBuilder(existingEntity)
            } else {
                Entity.newBuilder(key)
            }

            val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)

            val providerEntityBuilder = FullEntity.newBuilder()
            providerEntityBuilder.set("accessToken", accessToken)
            if (refreshToken != null) {
                providerEntityBuilder.set("refreshToken", refreshToken)
            }
            providerEntityBuilder.set("expiresAt", expiresAt)
            providerEntityBuilder.set("updatedAt", System.currentTimeMillis())

            builder.set(provider, EntityValue.of(providerEntityBuilder.build()))
            transaction.put(builder.build())
            transaction.commit()
            println("Token saved successfully.")
        } catch (e: Exception) {
            println("Error saving token: ${e.message}")
            if (transaction.isActive) transaction.rollback()
        }
    }

    /** Gets a valid Access Token. If expired, it refreshes it automatically. */
    suspend fun getValidToken(
            userId: String,
            provider: String,
            clientId: String,
            clientSecret: String
    ): String? {
        // 1. Intentar obtener el documento del usuario
        val keyFactory = datastore.newKeyFactory().setKind(KIND)
        val key = keyFactory.newKey(userId)
        val entity = datastore.get(key) ?: return null

        if (!entity.contains(provider)) return null

        // 2. Extraer los datos de la red social específica (twitter o linkedin)
        val providerEntity = entity.getEntity<IncompleteKey>(provider)

        // 3. Mapear a una estructura local para trabajar fácilmente
        val accessToken = providerEntity.getString("accessToken")
        val storedRefreshToken = if (providerEntity.contains("refreshToken")) providerEntity.getString("refreshToken") else null
        val expiresAt = providerEntity.getLong("expiresAt") // Timestamp en milisegundos

        // 4. Verificar si ha expirado (usando un margen de 60 segundos)
        // Convertimos milisegundos a segundos para comparar con epochSecond
        val now = Instant.now().epochSecond
        val expiryInSeconds = expiresAt / 1000

        if (now > (expiryInSeconds - 60)) {
            // El token ha expirado o va a expirar pronto
            if (storedRefreshToken == null) return null // No podemos refrescar sin refresh_token

            // Creamos un objeto temporal para la función de refresco
            val dataToRefresh =
                    OAuthTokenData(
                            provider = provider,
                            accessToken = accessToken,
                            refreshToken = storedRefreshToken,
                            expiresAt = expiresAt
                    )

            return refreshToken(userId, dataToRefresh, clientId, clientSecret)
        }

        return accessToken
    }

    private suspend fun refreshToken(
            userId: String,
            oldToken: OAuthTokenData,
            clientId: String,
            clientSecret: String
    ): String? {
        if (oldToken.refreshToken == null) return null // Cannot refresh

        val url =
                if (oldToken.provider == "twitter") "https://api.twitter.com/2/oauth2/token"
                else "https://www.linkedin.com/oauth/v2/accessToken"

        try {
            val response: TokenRefreshResponse =
                    httpClient
                            .submitForm(
                                    url = url,
                                    formParameters =
                                            Parameters.build {
                                                append("grant_type", "refresh_token")
                                                append("refresh_token", oldToken.refreshToken)
                                                append("client_id", clientId)
                                                append("client_secret", clientSecret)
                                            }
                            )
                            .body()

            // Update Store
            saveToken(
                    userId,
                    oldToken.provider,
                    response.accessToken,
                    response.refreshToken ?: oldToken.refreshToken,
                    response.expiresIn
            )
            return response.accessToken
        } catch (e: Exception) {
            println("Failed to refresh token: ${e.message}")
            return null
        }
    }
}



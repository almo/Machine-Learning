package com.catharsis.ai4media.ai4mediaserver

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

object TwitterConnector {
    private val logger = LoggerFactory.getLogger(TwitterConnector::class.java)

    private val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }

    suspend fun publishToTwitterTimeline(
            userId: String,
            textContent: String,
            urlContent: String?,
            tags: List<String>
    ): String {
        logger.info("Attempting to publish to Twitter timeline for user: {}", userId)

        val token =
                TokenService.getValidToken(
                        userId = userId,
                        provider = "twitter",
                        clientId = AppConfig.twitterClientId,
                        clientSecret = AppConfig.twitterClientSecret
                )
        if (token == null) {
            logger.error("Twitter Token Error: not found valid token for user {}", userId)
            throw Exception("Twitter Token Error: not found valid token for user $userId")
        }

        val combinedText = buildString {
            append(textContent)
            if (!urlContent.isNullOrBlank()) {
                append("\n\n$urlContent")
            }
            if (tags.isNotEmpty()) {
                append("\n\n")
                append(tags.joinToString(" ") { "$it" })
            }
        }

        val requestBody = buildJsonObject { put("text", combinedText) }

        val postResponse =
                httpClient.post("https://api.twitter.com/2/tweets") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

        if (!postResponse.status.isSuccess()) {
            val errorBody = postResponse.bodyAsText()
            logger.error("Error publishing to Twitter (Status: {}): {}", postResponse.status, errorBody)
            throw Exception("Error publishing to Twitter: $errorBody")
        }

        val responseObj = postResponse.body<JsonObject>()
        val dataObj = responseObj["data"]?.jsonObject
        val tweetId = dataObj?.get("id")?.jsonPrimitive?.content

        if (tweetId == null) {
            logger.error("Could not retrieve Tweet ID from response. Response was: {}", responseObj)
            throw Exception("Could not retrieve Tweet ID from response")
        }

        logger.info("Successfully published to Twitter timeline (Tweet ID: {}) for user: {}", tweetId, userId)
        return tweetId
    }
}

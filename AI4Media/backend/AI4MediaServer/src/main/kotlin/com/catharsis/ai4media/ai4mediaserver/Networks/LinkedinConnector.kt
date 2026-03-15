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

object LinkedinConnector {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val ORGANIZATION_URN = "urn:li:organization:77043213"
    private const val MOCK_IMAGE_URL = "https://storage.googleapis.com/social_media_engine/images/0000.jpg"
    private val MOCK_MENTIONS = listOf("urn:li:person:mock1", "urn:li:person:mock2")

    suspend fun publishToOrganizationTimeline(
        userId: String,
        textContent: String,
        urlContent: String?,
        tags: List<String>
    ): String {
        val token = TokenService.getValidToken(
            userId = userId,
            provider = "linkedin",
            clientId = AppConfig.linkedinClientId,
            clientSecret = AppConfig.linkedinClientSecret
        ) ?: throw Exception("No se encontró un token válido de LinkedIn para el usuario $userId")

        val combinedText = buildString {
            append(textContent)
            if (!urlContent.isNullOrBlank()) {
                append("\n\nEnlace: $urlContent")
            }
            if (tags.isNotEmpty()) {
                append("\n\n")
                append(tags.joinToString(" ") { "#$it" })
            }
            append("\n\n")
            append("Menciones: ${MOCK_MENTIONS.joinToString(" ") { "@$it" }}")
        }

        val requestBody = buildJsonObject {
            put("author", ORGANIZATION_URN)
            put("lifecycleState", "PUBLISHED")
            putJsonObject("specificContent") {
                putJsonObject("com.linkedin.ugc.ShareContent") {
                    putJsonObject("shareCommentary") {
                        put("text", combinedText)
                    }
                    // Usar ARTICLE para que LinkedIn se encargue de raspar ("scrape") la URL
                    put("shareMediaCategory", "ARTICLE")
                    putJsonArray("media") {
                        addJsonObject {
                            put("status", "READY")
                            put("originalUrl", if (!urlContent.isNullOrBlank()) urlContent else MOCK_IMAGE_URL)
                        }
                    }
                }
            }
            putJsonObject("visibility") {
                put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC")
            }
        }

        val postResponse = httpClient.post("https://api.linkedin.com/v2/ugcPosts") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("X-Restli-Protocol-Version", "2.0.0")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (!postResponse.status.isSuccess()) {
            val errorBody = postResponse.bodyAsText()
            throw Exception("Error al publicar en la organización (LinkedIn): $errorBody")
        }

        val responseObj = postResponse.body<JsonObject>()
        return responseObj["id"]?.jsonPrimitive?.content 
            ?: throw Exception("No se pudo obtener el URN de la publicación de organización")
    }

    suspend fun shareToUserTimeline(
        userId: String,
        originalPostUrn: String,
        comment: String,
        tags: List<String>
    ): String {
        val token = TokenService.getValidToken(
            userId = userId,
            provider = "linkedin",
            clientId = AppConfig.linkedinClientId,
            clientSecret = AppConfig.linkedinClientSecret
        ) ?: throw Exception("No se encontró un token válido de LinkedIn para el usuario $userId")

        // Recuperar el URN personal del usuario
        val userInfoResponse = httpClient.get("https://api.linkedin.com/v2/userinfo") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        if (!userInfoResponse.status.isSuccess()) {
            throw Exception("Fallo al obtener info del usuario de LinkedIn: ${userInfoResponse.bodyAsText()}")
        }
        
        val userInfo = userInfoResponse.body<JsonObject>()
        val personId = userInfo["sub"]?.jsonPrimitive?.content 
            ?: throw Exception("No se pudo obtener el identificador 'sub' del usuario de LinkedIn")
        
        val authorUrn = "urn:li:person:$personId"

        val combinedComment = buildString {
            append(comment)
            if (tags.isNotEmpty()) {
                append("\n\n")
                append(tags.joinToString(" ") { "#$it" })
            }
            append("\n\n")
            append("Menciones: ${MOCK_MENTIONS.joinToString(" ") { "@$it" }}")
        }

        val requestBody = buildJsonObject {
            put("author", authorUrn)
            put("lifecycleState", "PUBLISHED")
            putJsonObject("specificContent") {
                putJsonObject("com.linkedin.ugc.ShareContent") {
                    putJsonObject("shareCommentary") {
                        put("text", combinedComment)
                    }
                    put("shareMediaCategory", "ARTICLE")
                    putJsonArray("media") {
                        addJsonObject {
                            put("status", "READY")
                            put("originalUrl", "https://www.linkedin.com/feed/update/$originalPostUrn")
                        }
                    }
                }
            }
            putJsonObject("visibility") {
                put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC")
            }
        }

        val postResponse = httpClient.post("https://api.linkedin.com/v2/ugcPosts") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("X-Restli-Protocol-Version", "2.0.0")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (!postResponse.status.isSuccess()) {
            val errorBody = postResponse.bodyAsText()
            throw Exception("Error al compartir en el perfil de usuario (LinkedIn): $errorBody")
        }

        val responseObj = postResponse.body<JsonObject>()
        return responseObj["id"]?.jsonPrimitive?.content 
            ?: throw Exception("No se pudo obtener el URN de la publicación compartida")
    }
}
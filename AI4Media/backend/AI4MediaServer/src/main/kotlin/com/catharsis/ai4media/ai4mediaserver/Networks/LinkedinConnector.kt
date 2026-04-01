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
import kotlinx.coroutines.*

object LinkedinConnector {
    private val logger = LoggerFactory.getLogger(LinkedinConnector::class.java)

    private val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }

    private const val ORGANIZATION_URN = "urn:li:organization:77043213"
    private const val USER_URN = "urn:li:person:RtKv3HcbdP"
    private const val MOCK_IMAGE_URL =
            "https://storage.googleapis.com/social_media_engine/images/0010.jpg"
    private val MOCK_MENTIONS = listOf("urn:li:person:mock1", "urn:li:person:mock2")

    suspend fun publishToOrganizationTimeline(
            userId: String,
            textContent: String,
            urlContent: String?,
            tags: List<String>
    ): String {
        logger.info("Attempting to publish to LinkedIn Organization timeline for user: {}", userId)

        val token =
                TokenService.getValidToken(
                        userId = userId,
                        provider = "linkedin",
                        clientId = AppConfig.linkedinClientId,
                        clientSecret = AppConfig.linkedinClientSecret
                )
        if (token == null) {
            logger.error("LinkedIn Token Error: not found valid token for user {}", userId)
            throw Exception("LinkedIn Token Error: not found valid token for user $userId")
        }

        val combinedText = buildString {
            append(textContent)
            
            append("\n\n🔗 Full link in the first comment below 👇")

            if (tags.isNotEmpty()) {
                append("\n\n")
                append(tags.joinToString(" ") { "$it" })
            }
        }

        logger.info("Downloading image from bucket: {}", MOCK_IMAGE_URL)
        val imageResponse = httpClient.get(MOCK_IMAGE_URL)
        if (!imageResponse.status.isSuccess()) {
            throw Exception("Failed to download image from bucket: ${imageResponse.status}")
        }
        val imageBytes = imageResponse.body<ByteArray>()

        val assetUrn = uploadImage(token, ORGANIZATION_URN, imageBytes)

        val requestBody = buildJsonObject {
            put("author", ORGANIZATION_URN)
            put("lifecycleState", "PUBLISHED")
            putJsonObject("specificContent") {
                putJsonObject("com.linkedin.ugc.ShareContent") {
                    putJsonObject("shareCommentary") { put("text", combinedText) }
                    put("shareMediaCategory", "IMAGE")
                    putJsonArray("media") {
                        addJsonObject {
                            put("status", "READY")
                            put("media", assetUrn)
                        }
                    }
                }
            }
            putJsonObject("visibility") {
                put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC")
            }
        }

        val postResponse =
                httpClient.post("https://api.linkedin.com/v2/ugcPosts") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header("X-Restli-Protocol-Version", "2.0.0")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

        if (!postResponse.status.isSuccess()) {
            val errorBody = postResponse.bodyAsText()
            logger.error("Error publishing to LinkedIn organization (Status: {}): {}", postResponse.status, errorBody)
            throw Exception("Error al publicar en la organización (LinkedIn): $errorBody")
        }

        val responseObj = postResponse.body<JsonObject>()
        val postId = responseObj["id"]?.jsonPrimitive?.content

        if (postId == null) {
            logger.error("Could not retrieve URN from LinkedIn organization post response. Response was: {}", responseObj)
            throw Exception("No se pudo obtener el URN de la publicación de organización")
        }

        if (!urlContent.isNullOrBlank()) {
            val commentBody = buildJsonObject {
                put("actor", ORGANIZATION_URN)
                put("object", postId)
                putJsonObject("message") { put("text", "Read the full analysis here: $urlContent") }
            }

            val encodedPostId = java.net.URLEncoder.encode(postId, "UTF-8")
            
            val commentResponse =
                    httpClient.post("https://api.linkedin.com/v2/socialActions/$encodedPostId/comments") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header("X-Restli-Protocol-Version", "2.0.0")
                        contentType(ContentType.Application.Json)
                        setBody(commentBody)
                    }

            if (!commentResponse.status.isSuccess()) {
                val errorBody = commentResponse.bodyAsText()
                logger.error("Error adding comment with link to LinkedIn post (Status: {}): {}", commentResponse.status, errorBody)
                throw Exception("Error al añadir el comentario con enlace (LinkedIn): $errorBody")
            }
        }

        logger.info("Successfully published to LinkedIn Organization timeline (Post ID: {}) for user: {}", postId, userId)

        return postId
    }

    suspend fun shareToUserTimeline(
            userId: String,
            originalPostUrn: String,
            comment: String,
            tags: List<String>,
            authorUrn: String = USER_URN // www.linkedin.com/in/almo
    ): String {
        logger.info("Attempting to share post to user timeline for user: {}", userId)

        val token =
                TokenService.getValidToken(
                        userId = userId,
                        provider = "linkedin",
                        clientId = AppConfig.linkedinClientId,
                        clientSecret = AppConfig.linkedinClientSecret
                )
        if (token == null) {
            logger.error("LinkedIn Token Error: not found valid token for user {}", userId)
            throw Exception("No se encontró un token válido de LinkedIn para el usuario $userId")
        }

        // Recuperar el URN personal del usuario
        val userInfoResponse =
                httpClient.get("https://api.linkedin.com/v2/userinfo") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

        if (!userInfoResponse.status.isSuccess()) {
            val errorBody = userInfoResponse.bodyAsText()
            logger.error("Failed to fetch LinkedIn user info for user {} (Status: {}): {}", userId, userInfoResponse.status, errorBody)
            throw Exception("Fallo al obtener info del usuario de LinkedIn: $errorBody")
        }

        val userInfo = userInfoResponse.body<JsonObject>()
        val personId = userInfo["sub"]?.jsonPrimitive?.content

        if (personId == null) {
            logger.error("Could not retrieve 'sub' identifier from LinkedIn user info response for user: {}", userId)
            throw Exception("No se pudo obtener el identificador 'sub' del usuario de LinkedIn")
        }

        val combinedComment = buildString {
            append(comment)
            if (tags.isNotEmpty()) {
                append("\n\n")
                append(tags.joinToString(" ") { "$it" })
            }
        }

        val requestBody = buildJsonObject {
            put("author", authorUrn)
            put("lifecycleState", "PUBLISHED")
            putJsonObject("specificContent") {
                putJsonObject("com.linkedin.ugc.ShareContent") {
                    putJsonObject("shareCommentary") { put("text", combinedComment) }
                    put("shareMediaCategory", "ARTICLE")
                    putJsonArray("media") {
                        addJsonObject {
                            put("status", "READY")
                            put(
                                    "originalUrl",
                                    "https://www.linkedin.com/feed/update/$originalPostUrn"
                            )
                        }
                    }
                }
            }
            putJsonObject("visibility") {
                put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC")
            }
        }

        val postResponse =
                httpClient.post("https://api.linkedin.com/v2/ugcPosts") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header("X-Restli-Protocol-Version", "2.0.0")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

        if (!postResponse.status.isSuccess()) {
            val errorBody = postResponse.bodyAsText()
            logger.error("Error sharing to LinkedIn user timeline (Status: {}): {}", postResponse.status, errorBody)
            throw Exception("Error al compartir en el perfil de usuario (LinkedIn): $errorBody")
        }

        val responseObj = postResponse.body<JsonObject>()
        val sharedPostId = responseObj["id"]?.jsonPrimitive?.content
        
        if (sharedPostId == null) {
            logger.error("Could not retrieve URN from shared post response. Response was: {}", responseObj)
            throw Exception("No se pudo obtener el URN de la publicación compartida")
        }
        
        logger.info("Successfully shared post to LinkedIn user timeline (Shared Post ID: {}) for user: {}", sharedPostId, userId)
        return sharedPostId
    }

    suspend fun uploadImage(
            token: String,
            ownerUrn: String,
            imageBytes: ByteArray
    ): String {
        logger.info("Step 1: Registering image upload for owner: {}", ownerUrn)
        
        val registerRequestBody = buildJsonObject {
            putJsonObject("registerUploadRequest") {
                putJsonArray("recipes") {
                    add("urn:li:digitalmediaRecipe:feedshare-image")
                }
                put("owner", ownerUrn)
                putJsonArray("serviceRelationships") {
                    addJsonObject {
                        put("relationshipType", "OWNER")
                        put("identifier", "urn:li:userGeneratedContent")
                    }
                }
            }
        }

        val registerResponse = httpClient.post("https://api.linkedin.com/v2/assets?action=registerUpload") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("X-Restli-Protocol-Version", "2.0.0")
            contentType(ContentType.Application.Json)
            setBody(registerRequestBody)
        }

        if (!registerResponse.status.isSuccess()) {
            val errorBody = registerResponse.bodyAsText()
            logger.error("Error registering image upload (Status: {}): {}", registerResponse.status, errorBody)
            throw Exception("Failed to register image upload on LinkedIn: $errorBody")
        }

        val registerObj = registerResponse.body<JsonObject>()
        val valueObj = registerObj["value"]?.jsonObject
            ?: throw Exception("Invalid response format from LinkedIn asset registration")
        
        val assetUrn = valueObj["asset"]?.jsonPrimitive?.content
            ?: throw Exception("Could not extract asset URN from LinkedIn response")
        
        val uploadUrl = valueObj["uploadMechanism"]?.jsonObject
            ?.get("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest")?.jsonObject
            ?.get("uploadUrl")?.jsonPrimitive?.content
            ?: throw Exception("Could not extract upload URL from LinkedIn response")

        logger.info("Step 2: Uploading image binary to LinkedIn (Asset URN: {})", assetUrn)
        
        val uploadResponse = httpClient.put(uploadUrl) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(imageBytes)
        }

        if (!uploadResponse.status.isSuccess()) {
            val errorBody = uploadResponse.bodyAsText()
            logger.error("Error uploading image binary (Status: {}): {}", uploadResponse.status, errorBody)
            throw Exception("Failed to upload image binary to LinkedIn: $errorBody")
        }

        logger.info("Image successfully uploaded to LinkedIn (Asset URN: {})", assetUrn)
        
        // Step 3: Returns the asset URN to be used in creating the UGC Post media array.
        return assetUrn
    }
}

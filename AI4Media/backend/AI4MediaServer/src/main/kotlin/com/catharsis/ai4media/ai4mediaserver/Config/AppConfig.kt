package com.catharsis.ai4media.ai4mediaserver

import java.time.ZoneId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


/**
 * Central configuration object for the AI4MediaServer application.
 *
 * This object is responsible for loading and exposing configuration properties
 * required by the application. It retrieves non-sensitive configuration from
 * environment variables and sensitive credentials (secrets) from Google Cloud
 * Secret Manager.
 */
object AppConfig {
    /**
     * The Google Cloud Project ID where the application is running.
     * Loaded from the `GOOGLE_CLOUD_PROJECT` environment variable.
     */
    val projectId: String

    /**
     * The base URL of the deployed application.
     * Used for constructing callback URLs for OAuth flows.
     * Loaded from the `APPENGINE_BASE_URL` environment variable.
     */
    val baseUrl: String

    /**
     * The Google Cloud Service Account email used for authentication.
     * Retrieved from Secret Manager ("GOOGLE_CLOUD_SERVICE_ACCOUNT").
     */
    val serviceAccount: String

    /**
     * The Google Cloud region ID (e.g., "us-central1").
     * Retrieved from Secret Manager ("CLOUD_LOCATION_ID").
     */
    val cloudLocationId: String

    /**
     * The ID of the Cloud Tasks queue used for scheduling background jobs.
     * Retrieved from Secret Manager ("CLOUD_TASKS_QUEUE_ID").
     */
    val cloudTasksQueueId: String

    /**
     * The OAuth 2.0 Client ID for Twitter authentication.
     * Retrieved from Secret Manager ("TWITTER_CLIENT_ID").
     */
    val twitterClientId: String

    /**
     * The OAuth 2.0 Client Secret for Twitter authentication.
     * Retrieved from Secret Manager ("TWITTER_CLIENT_SECRET").
     */
    val twitterClientSecret: String

    /**
     * The OAuth 2.0 Client ID for LinkedIn authentication.
     * Retrieved from Secret Manager ("LINKEDIN_CLIENT_ID").
     */
    val linkedinClientId: String

    /**
     * The OAuth 2.0 Client Secret for LinkedIn authentication.
     * Retrieved from Secret Manager ("LINKEDIN_CLIENT_SECRET").
     */
    val linkedinClientSecret: String

    /**
     * The secret string used for signing session data.
     * Retrieved from Secret Manager ("OAUTH_SESSION_SECRET_KEY").
     */
    val sessionSecretString: String

    /**
     * The encryption key used for securing session data.
     * Retrieved from Secret Manager ("SECRET_ENCRYPT_KEY").
     */
    val sessionEncryptKey: String

    /**
     * The timezone of the application
     */
    val timeZone : ZoneId

    /**
     * The baseline cutoff in days for syncing RSS news.
     */
    val rssNewsBaselineCutoffDays: Long

    /**
     * The retention limit in days for keeping old RSS news.
     */
    val rssNewsRetentionDays: Long

    /**
     * The Vertex AI generative model identifier (e.g. "gemini-2.5-flash").
     * Loaded from VERTEX_AI_MODEL env var or defaults to "gemini-2.5-flash".
     */
    val vertexAiModel: String

    init {
        projectId = System.getenv("GOOGLE_CLOUD_PROJECT") ?: throw IllegalStateException("GOOGLE_CLOUD_PROJECT env var not set")
        baseUrl = System.getenv("APPENGINE_BASE_URL")?.removeSuffix("/") ?: throw IllegalStateException("APPENGINE_BASE_URL env var not set")

        val ai4mediaSecretJson = SecretManager.getSecret(projectId, "AI4MEDIA")
        val secrets = Json.decodeFromString<Map<String, String>>(ai4mediaSecretJson)

        cloudLocationId      = secrets["CLOUD_LOCATION_ID"] ?: throw IllegalStateException("CLOUD_LOCATION_ID missing in AI4MEDIA secret")
        cloudTasksQueueId    = secrets["CLOUD_TASKS_QUEUE_ID"] ?: throw IllegalStateException("CLOUD_TASKS_QUEUE_ID missing in AI4MEDIA secret")
        serviceAccount       = secrets["GOOGLE_CLOUD_SERVICE_ACCOUNT"] ?: throw IllegalStateException("GOOGLE_CLOUD_SERVICE_ACCOUNT missing in AI4MEDIA secret")
        twitterClientId      = secrets["TWITTER_CLIENT_ID"] ?: throw IllegalStateException("TWITTER_CLIENT_ID missing in AI4MEDIA secret")
        twitterClientSecret  = secrets["TWITTER_CLIENT_SECRET"] ?: throw IllegalStateException("TWITTER_CLIENT_SECRET missing in AI4MEDIA secret")
        linkedinClientId     = secrets["LINKEDIN_CLIENT_ID"] ?: throw IllegalStateException("LINKEDIN_CLIENT_ID missing in AI4MEDIA secret")
        linkedinClientSecret = secrets["LINKEDIN_CLIENT_SECRET"] ?: throw IllegalStateException("LINKEDIN_CLIENT_SECRET missing in AI4MEDIA secret")
        sessionSecretString  = secrets["OAUTH_SESSION_SECRET_KEY"] ?: throw IllegalStateException("OAUTH_SESSION_SECRET_KEY missing in AI4MEDIA secret")
        sessionEncryptKey    = secrets["SECRET_ENCRYPT_KEY"] ?: throw IllegalStateException("SECRET_ENCRYPT_KEY missing in AI4MEDIA secret")

        timeZone             =   ZoneId.of("Europe/Zurich")
        rssNewsBaselineCutoffDays = System.getenv("RSS_NEWS_BASELINE_CUTOFF_DAYS")?.toLongOrNull() ?: 45L
        rssNewsRetentionDays = System.getenv("RSS_NEWS_RETENTION_DAYS")?.toLongOrNull() ?: 30L
        vertexAiModel        = System.getenv("VERTEX_AI_MODEL")?.ifBlank { null } ?: "gemini-2.5-flash-lite"
    }
}
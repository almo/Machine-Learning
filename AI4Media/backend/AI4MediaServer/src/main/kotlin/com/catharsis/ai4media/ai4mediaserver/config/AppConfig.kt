package com.catharsis.ai4media.ai4mediaserver

import java.time.ZoneId

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

    init {
        projectId = System.getenv("GOOGLE_CLOUD_PROJECT") ?: throw IllegalStateException("GOOGLE_CLOUD_PROJECT env var not set")
        baseUrl = System.getenv("APPENGINE_BASE_URL")?.removeSuffix("/") ?: throw IllegalStateException("APPENGINE_BASE_URL env var not set")
        cloudLocationId      =   SecretManager.getSecret(projectId, "CLOUD_LOCATION_ID") 
        cloudTasksQueueId    =   SecretManager.getSecret(projectId, "CLOUD_TASKS_QUEUE_ID") 
        serviceAccount       =   SecretManager.getSecret(projectId, "GOOGLE_CLOUD_SERVICE_ACCOUNT") 
        twitterClientId      =   SecretManager.getSecret(projectId, "TWITTER_CLIENT_ID") 
        twitterClientSecret  =   SecretManager.getSecret(projectId, "TWITTER_CLIENT_SECRET") 
        linkedinClientId     =   SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_ID") 
        linkedinClientSecret =   SecretManager.getSecret(projectId, "LINKEDIN_CLIENT_SECRET") 
        sessionSecretString  =   SecretManager.getSecret(projectId, "OAUTH_SESSION_SECRET_KEY") 
        sessionEncryptKey    =   SecretManager.getSecret(projectId, "SECRET_ENCRYPT_KEY") 
        timeZone             =   ZoneId.of("Europe/Zurich")
    }
}
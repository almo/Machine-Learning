package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName

/**
 * Utility object for interacting with Google Cloud Secret Manager.
 *
 * This component is responsible for securely retrieving sensitive configuration
 * values, such as API keys and tokens, from the Google Cloud Secret Manager service.
 * It abstracts the underlying gRPC client details.
 */
object SecretManager {

    /**
     * The [SecretManagerServiceClient] instance used to make API calls.
     * Initialized lazily to ensure resources are allocated only when needed.
     */
    private val client: SecretManagerServiceClient by lazy {
        SecretManagerServiceClient.create()
    }

    /**
     * Retrieves the payload of a specific secret version.
     *
     * This method fetches the secret data synchronously. It assumes the application
     * has the necessary IAM permissions (Secret Manager Secret Accessor) to access
     * the requested resource.
     *
     * @param projectId The Google Cloud Project ID (e.g., "my-project-123").
     * @param secretId The name of the secret to retrieve (e.g., "database-password").
     * @param versionId The version of the secret to fetch. Defaults to "latest".
     * @return The secret payload as a UTF-8 [String].
     * @throws com.google.api.gax.rpc.ApiException If the secret cannot be accessed or found.
     */
    fun getSecret(projectId: String, secretId: String, versionId: String = "latest"): String {
        val secretVersionName = SecretVersionName.of(projectId, secretId, versionId)
        val response = client.accessSecretVersion(secretVersionName)
        return response.payload.data.toStringUtf8()
    }
}
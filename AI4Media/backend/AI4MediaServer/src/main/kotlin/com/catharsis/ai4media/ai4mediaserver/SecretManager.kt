package com.catharsis.ai4media.ai4mediaserver

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName

object SecretManager {

    /**
     * Retrieves a secret payload from Google Secret Manager.
     * @param projectId Your Google Cloud Project ID
     * @param secretId The name of the secret (e.g., "FIREBASE_API_KEY")
     * @param version The version to fetch (default is "latest")
     */
    fun getSecret(projectId: String, secretId: String, version: String = "latest"): String {
        // Initialize the client. The try-use block ensures it closes resources automatically.
        // On App Engine, this automatically finds the correct service account credentials.
        SecretManagerServiceClient.create().use { client ->
            val secretVersionName = SecretVersionName.of(projectId, secretId, version)

            // Access the secret version
            val response = client.accessSecretVersion(secretVersionName)

            // Return the payload as a UTF-8 string
            return response.payload.data.toStringUtf8()
        }
    }
}
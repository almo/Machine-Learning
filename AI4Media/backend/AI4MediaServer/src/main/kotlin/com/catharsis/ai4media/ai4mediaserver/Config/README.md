# Configuration Package (`com.catharsis.ai4media.ai4mediaserver.Config`)

## Overview

This directory contains the central configuration and secret management infrastructure for the **AI4MediaServer** backend application.

The package is responsible for aggregating runtime settings from standard environment variables and securely loading sensitive credentials, API keys, and session secrets from **Google Cloud Secret Manager**.

---

## Files in this Directory

### 1. [`AppConfig.kt`](./AppConfig.kt)
[`AppConfig`](./AppConfig.kt#L16) is a singleton object that serves as the central configuration registry for the application.

- **Initialization & Loading Strategy**:
  - Validates and loads non-sensitive runtime parameters directly from system environment variables.
  - Calls [`SecretManager`](./SecretManager.kt#L13) to retrieve the JSON-encoded `AI4MEDIA` secret payload and decodes it using `kotlinx.serialization`.
- **Exposed Configuration**:
  - **Google Cloud & Infrastructure**:
    - `projectId` (`GOOGLE_CLOUD_PROJECT` env var): Google Cloud project identifier.
    - `baseUrl` (`APPENGINE_BASE_URL` env var): Base application URL for OAuth redirect URLs.
    - `cloudLocationId` (`CLOUD_LOCATION_ID` secret): Google Cloud region (e.g., `us-central1`).
    - `cloudTasksQueueId` (`CLOUD_TASKS_QUEUE_ID` secret): Target Cloud Tasks queue identifier for background jobs.
    - `serviceAccount` (`GOOGLE_CLOUD_SERVICE_ACCOUNT` secret): Google Cloud Service Account email.
  - **OAuth Credentials**:
    - `twitterClientId` & `twitterClientSecret`: Client ID and secret for Twitter OAuth 2.0.
    - `linkedinClientId` & `linkedinClientSecret`: Client ID and secret for LinkedIn OAuth 2.0.
  - **Security & Session Management**:
    - `sessionSecretString` (`OAUTH_SESSION_SECRET_KEY` secret): Secret string for signing session data.
    - `sessionEncryptKey` (`SECRET_ENCRYPT_KEY` secret): Encryption key for securing sensitive session data.
  - **Application Settings**:
    - `timeZone`: Default timezone (`Europe/Zurich`).
    - `rssNewsBaselineCutoffDays` (`RSS_NEWS_BASELINE_CUTOFF_DAYS` env var, defaults to 45 days): Sync baseline window for RSS news.
    - `rssNewsRetentionDays` (`RSS_NEWS_RETENTION_DAYS` env var, defaults to 30 days): Retention limit for keeping old RSS news.

---

### 2. [`SecretManager.kt`](./SecretManager.kt)
[`SecretManager`](./SecretManager.kt#L13) is a utility singleton providing an abstraction layer over the Google Cloud Secret Manager client library.

- **Key Responsibilities**:
  - **Lazy Client Initialization**: Lazily creates and reuses a [`SecretManagerServiceClient`](https://cloud.google.com/secret-manager/docs) instance.
  - **Secret Retrieval**: Provides [`getSecret(projectId, secretId, versionId = "latest")`](./SecretManager.kt#L36), which fetches secret versions synchronously and parses payloads to UTF-8 strings.

---

## Configuration Mapping Reference

| Configuration Key | Source | Target Property | Description |
| :--- | :--- | :--- | :--- |
| `GOOGLE_CLOUD_PROJECT` | Environment Variable | `AppConfig.projectId` | GCP Project ID |
| `APPENGINE_BASE_URL` | Environment Variable | `AppConfig.baseUrl` | Public URL prefix |
| `RSS_NEWS_BASELINE_CUTOFF_DAYS` | Environment Variable (optional, default: 45) | `AppConfig.rssNewsBaselineCutoffDays` | RSS sync cutoff |
| `RSS_NEWS_RETENTION_DAYS` | Environment Variable (optional, default: 30) | `AppConfig.rssNewsRetentionDays` | RSS retention limit |
| `CLOUD_LOCATION_ID` | `AI4MEDIA` GCP Secret JSON | `AppConfig.cloudLocationId` | GCP Region |
| `CLOUD_TASKS_QUEUE_ID` | `AI4MEDIA` GCP Secret JSON | `AppConfig.cloudTasksQueueId` | Cloud Tasks queue name |
| `GOOGLE_CLOUD_SERVICE_ACCOUNT` | `AI4MEDIA` GCP Secret JSON | `AppConfig.serviceAccount` | Service Account email |
| `TWITTER_CLIENT_ID` | `AI4MEDIA` GCP Secret JSON | `AppConfig.twitterClientId` | Twitter OAuth Client ID |
| `TWITTER_CLIENT_SECRET` | `AI4MEDIA` GCP Secret JSON | `AppConfig.twitterClientSecret` | Twitter OAuth Client Secret |
| `LINKEDIN_CLIENT_ID` | `AI4MEDIA` GCP Secret JSON | `AppConfig.linkedinClientId` | LinkedIn OAuth Client ID |
| `LINKEDIN_CLIENT_SECRET` | `AI4MEDIA` GCP Secret JSON | `AppConfig.linkedinClientSecret` | LinkedIn OAuth Client Secret |
| `OAUTH_SESSION_SECRET_KEY` | `AI4MEDIA` GCP Secret JSON | `AppConfig.sessionSecretString` | Session signing secret key |
| `SECRET_ENCRYPT_KEY` | `AI4MEDIA` GCP Secret JSON | `AppConfig.sessionEncryptKey` | Session encryption key |

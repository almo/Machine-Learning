# AI4Media Server (`com.catharsis.ai4media.ai4mediaserver`)

The `ai4mediaserver` package is the core backend application for **AI4Media**, an intelligent content aggregation, generative AI copywriting, and automated social media scheduling platform built on Kotlin and the [Ktor](https://ktor.io/) asynchronous framework.

The server orchestrates the complete lifecycle of social media marketing: from continuous RSS feed ingestion and heuristic article scraping, to LLM-powered multi-persona post generation via Google Cloud Vertex AI (Gemini), intelligent audience-optimized time distribution, and reliable distributed publishing to LinkedIn and Twitter/X via Google Cloud Tasks.

---

## 🏛️ System Architecture

```mermaid
flowchart TD
    subgraph Ingestion["1. Ingestion & Curation"]
        CRON["App Engine Cron<br/>(/internal/cron/news-sync)"] --> NR["NewsRoutes.kt"]
        NR --> SC["NewsScrapper / WebScrapper"]
        SC --> DS_NEWS[("Datastore (News)")]
    end

    subgraph AI["2. AI Copy Generation"]
        USER["Client / User"] -->|POST /api/ai/generate| AIR["AiGenerateRoutes.kt"]
        AIR --> SCRAPE["Web Scraping & OpenGraph Extraction"]
        SCRAPE --> GEMINI["VertexAI (Gemini 3.5 Flash Lite)"]
        GEMINI -->|Structured JSON Copy| AIR
    end

    subgraph Scheduling["3. Scheduling & Queue Management"]
        USER -->|POST /schedule| RR["Routing.kt"]
        RR -->|Auto Sweet-Spot Alg| CT["CloudTasks.kt"]
        CT --> GCP_TASKS["Google Cloud Tasks Queue"]
        RR --> DS_POSTS[("Datastore (SocialContent)")]
    end

    subgraph Dispatch["4. Publishing & Delivery"]
        GCP_TASKS -->|POST /publish/{id}<br/>(OIDC Verified)| PUB["Routing.kt (publishPost)"]
        PUB --> AUTH["TokenService (Auto-Refresh)"]
        AUTH --> NET_LI["LinkedinConnector"]
        AUTH --> NET_TW["TwitterConnector"]
        NET_LI -->|UGC Post + Media + Comment| LI_API["LinkedIn API v2"]
        NET_TW -->|Tweet API v2| TW_API["Twitter/X API v2"]
    end
```

---

## 📁 Package Structure & Submodule Index

| Directory / File | Type | Description |
| :--- | :--- | :--- |
| [`Application.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Application.kt) | Core Application | Application entry point (`main`), Ktor server configuration, plugin installations (Compression, Content Negotiation, Encrypted Sessions, Authentication), and routing setup. |
| [`CloudTasks.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/CloudTasks.kt) | Cloud Integration | Utility singleton for scheduling HTTP worker tasks on Google Cloud Tasks with OIDC service-account authentication. |
| [`SocialContent.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt) | Domain Models | Core data structures representing posts, scheduling requests, post lifecycles (`PostStatus`), target platforms (`SocialNetwork`), and custom serializers. |
| [`Auth/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/README.md) | Submodule | OAuth 2.0 PKCE generation, Datastore token lifecycle management with automatic refresh, and Google OIDC token verification. |
| [`Config/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/README.md) | Submodule | Centralized runtime configuration (`AppConfig`) and Google Cloud Secret Manager integration (`SecretManager`). |
| [`Networks/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/README.md) | Submodule | Third-party social connectors for LinkedIn REST API v2 (UGC posts, media uploads, first comments) and Twitter/X API v2. |
| [`Routing/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/README.md) | Submodule | Ktor HTTP routing controllers handling AI generation, RSS feed sync, reading list management, social auth flows, and post scheduling/publishing. |
| [`Utils/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/README.md) | Submodule | Supporting infrastructure including Vertex AI (Gemini) client, Datastore repositories, Cloud Storage image signing, and web scraping utilities. |

---

## 🔍 Core Files Overview

### 1. [`Application.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Application.kt)

Serves as the central entry point for the Ktor CIO server. It boots Google Firebase Admin SDK, configures server-wide middleware plugins, and installs security and authentication protocols:

- **Firebase Admin SDK Initialization**: Initializes Firebase credentials via Application Default Credentials (ADC) for the configured project ID.
- **HTTP Compression Plugin (`Compression`)**:
  - Implements **Zstandard** (`zstd` level 3, priority 1.1), **Gzip** (priority 1.0), and **Deflate** (priority 0.9).
  - Enforces a 1 KB minimum size threshold and restricts compression to JSON, JavaScript, and Text content types.
  - Implements BREACH attack mitigation by restricting compression to non-cross-site requests originating from the trusted frontend.
- **Content Negotiation (`ContentNegotiation`)**: Configures Kotlinx Serialization JSON engine with `ignoreUnknownKeys`, `prettyPrint`, and `encodeDefaults`.
- **Encrypted Session Management (`Sessions`)**:
  - Uses `AI4MEDIA_SESSION` cookie with `httpOnly`, `secure`, and a 180-second TTL.
  - Secures session payloads using `SessionTransportTransformerEncrypt` with AES encryption and HMAC-SHA256 signing keys retrieved securely from `AppConfig`.
- **Authentication (`Authentication`)**:
  - `firebase-auth` (Bearer): Validates client Firebase ID tokens and sets `User(userId, email)` principal.
  - `google-cloud-tasks` (Bearer): Authenticates automated webhook callbacks from Google Cloud Tasks using [`TokenVerifier`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenVerifier.kt).
  - `auth-twitter` & `auth-linkedin` (OAuth 2.0): Manages OAuth 2.0 flows, integrating PKCE parameters (`code_challenge`, `code_challenge_method = S256`, `code_verifier`) for Twitter and organization/personal profile scopes for LinkedIn.
- **Routing Registration**: Wires all modular routing extension functions (`configureRouting`, `configureSourceRouting`, `configureAuthRouting`, `configureNewsRouting`, `configureScheduledContentRouting`, `configureReadingListRouting`, `configureAIGenerationRouting`).

---

### 2. [`CloudTasks.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/CloudTasks.kt)

Provides the [`CloudTasks`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/CloudTasks.kt#L12) singleton for enqueuing distributed asynchronous background tasks on Google Cloud Tasks:

- **`createHttpTask(...)`**:
  - Builds an HTTP POST request targeting the destination callback URL (`/publish/{id}`).
  - Sets up OpenID Connect (OIDC) authentication (`OidcToken`) using the configured service account email and base audience URL (`AppConfig.baseUrl`).
  - Converts the Java `Instant` scheduled execution timestamp into a Google Protobuf `Timestamp`.
  - Dispatches the task to the specified Cloud Tasks queue (`QueueName.of(projectId, locationId, queueId)`).

---

### 3. [`SocialContent.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt)

Defines the central domain models and serialization contracts governing social content:

- **Data Models**:
  - [`SocialContentRequest`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L42): Inbound scheduling payload containing text content, source URL, scheduled time string, tags, and target networks.
  - [`SocialContent`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L52): Complete domain representation stored in Datastore, including UUID, owner ID, text, context URL, target platform URN, creation and scheduled timestamps, tags, status, network, and profile.
- **Enums**:
  - [`PostStatus`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L68): Post lifecycle states (`DRAFT`, `SCHEDULED`, `AUTOSCHEDULED`, `PUBLISHED`, `FAILED`, `PUBLISHING`, `DELETED`).
  - [`SocialNetwork`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L87): Supported platforms (`LINKEDIN`, `TWITTER`).
  - [`SocialProfile`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L93): Profile target type (`PERSONAL`, `COMPANY`).
- **Custom Serializers**:
  - [`LocalDateTimeSerializer`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L14): Handles ISO-8601 formatting and parsing for `LocalDateTime`.
  - [`UUIDSerializer`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L20): Converts Java `UUID` instances to/from JSON strings.

---

## ⚡ Key Subsystems & Workflows

### 1. Authentication & Token Management Subsystem
- Located in [`Auth/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/README.md).
- Handles OAuth 2.0 PKCE security via [`oAuthPKCE`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/oAuthPKCE.kt#L9).
- Manages encrypted Datastore token persistence and transparent, preemptive token refreshes (with a 60-second safety window) via [`TokenService`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenService.kt#L35).
- Validates Google Cloud Tasks OIDC JWT tokens via [`TokenVerifier`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenVerifier.kt#L10).

### 2. Configuration & Secret Management
- Located in [`Config/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/README.md).
- Merges system environment variables (project ID, base URL, retention settings) with Google Cloud Secret Manager payloads (`AI4MEDIA` secret) via [`AppConfig`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/AppConfig.kt#L16) and [`SecretManager`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/SecretManager.kt#L13).

### 3. AI-Powered Copy Generation Pipeline
- Located in [`Routing/AiGenerateRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AiGenerateRoutes.kt) and [`Utils/VertexAI.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt).
- Scrapes input URLs, cleans HTML, and extracts OpenGraph images via [`NewsScraper`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/NewsScrapper.kt#L11) and [`ImageResolver`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/WebScrapper.kt#L5).
- Queries Gemini via [`GeminiClient`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt#L43) with injection-defended structured prompts, generating tailored copy for LinkedIn Company pages, LinkedIn Personal Thought Leadership bumps, and Twitter/X posts, alongside token/cost usage metrics.

### 4. Scheduling & Distribution Engine
- Located in [`Routing/Routing.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/Routing.kt).
- Supports three scheduling modes:
  1. `NOW`: Immediately publishes content to external platforms.
  2. `SCHEDULED`: Schedules execution at a user-specified ISO-8601 timestamp.
  3. `AUTOMATIC`: Executes a smart scheduling algorithm allocating posts to high-engagement daily windows ("Morning", "Lunch", "Commute", "Night") respecting user-configured daily publication quotas.
- Tasks are persisted in Datastore via [`DataStoreWrapper`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DataStore.kt#L14) and queued into Google Cloud Tasks via [`CloudTasks`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/CloudTasks.kt#L12).

### 5. Multi-Platform Social Connectors
- Located in [`Networks/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/README.md).
- [`LinkedinConnector`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/LinkedinConnector.kt#L21): Dispatches UGC posts, uploads image assets to LinkedIn Digital Media Asset services, and posts link comments under created media posts.
- [`TwitterConnector`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/TwitterConnector.kt#L13): Dispatches tweets via Twitter API v2 endpoints using authenticated bearer tokens.

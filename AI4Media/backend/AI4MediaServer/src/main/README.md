# AI4MediaServer Main Source Directory (`src/main`)

This directory houses the complete source code, deployment specifications, runtime configurations, and client-side web assets for the **AI4MediaServer** backend application.

The platform is an intelligent content aggregation, generative AI copywriting, and automated social media publishing system built on Kotlin and the [Ktor](https://ktor.io/) asynchronous framework, deployed to **Google Cloud App Engine Standard**.

---

## 📁 Directory Structure

```text
src/main/
├── appengine/                  # Google Cloud App Engine deployment descriptors & configs
│   ├── app.yaml                # Service definition, scaling, runtime & handlers
│   ├── cron.yaml               # App Engine cron job schedules (news sync)
│   ├── dispatch.yaml           # Traffic routing rules & custom domain mapping
│   ├── index.yaml              # Datastore composite indexes
│   └── README.md               # App Engine documentation
│
├── kotlin/                     # Backend application source code
│   └── com/catharsis/ai4media/ai4mediaserver/
│       ├── Application.kt      # Server bootstrap, Ktor engine, plugins & auth
│       ├── CloudTasks.kt       # Google Cloud Tasks asynchronous queue client
│       ├── SocialContent.kt    # Domain models, enums & serialization contracts
│       ├── README.md           # Backend package overview & architecture
│       ├── Auth/               # OAuth 2.0 PKCE, Datastore tokens & OIDC verifier
│       ├── Config/             # AppConfig & Google Cloud Secret Manager client
│       ├── Networks/           # Social connectors (LinkedIn v2 & Twitter/X v2)
│       ├── Routing/            # Ktor REST endpoint controllers & scheduler logic
│       └── Utils/              # Vertex AI, Datastore repo, Cloud Storage & scrapers
│
└── resources/                  # Runtime configurations & static web assets
    ├── application.conf        # Ktor HOCON server & deployment settings
    ├── logback.xml             # Logback console & GCP structured JSON loggers
    ├── README.md               # Resources directory documentation
    └── static/                 # Embedded frontend Single Page Application (SPA)
        ├── index.html          # HTML5 layout & Alpine.js template views
        ├── app.js              # Alpine.js reactive state manager & API client
        ├── auth.js             # Firebase Authentication client & Google OAuth
        ├── firebase-config.js  # Firebase modular SDK initialization
        ├── styles.css          # Styling, scrollbars & background patterns
        ├── favicon.ico         # Application favicon
        └── README.md           # Frontend Single Page Application documentation
```

---

## 🏛️ System Architecture & Data Flow

```mermaid
flowchart TD
    subgraph Client["Web Client (SPA)"]
        UI["Alpine.js Frontend<br/>(resources/static)"]
        FB_AUTH["Firebase Auth<br/>(Google Sign-In)"]
    end

    subgraph AppEngine["Google Cloud App Engine (backend)"]
        CONF["Ktor Engine & Application.kt<br/>(Java 21 / CIO / Port 8080)"]
        ROUTING["Routing Controllers<br/>(Routing/)"]
        CRON["Cron Service<br/>(cron.yaml: /internal/cron/news-sync)"]
    end

    subgraph GCP["Google Cloud Services"]
        SECRETS["Secret Manager<br/>(AI4MEDIA Secret)"]
        DATASTORE[("Cloud Datastore<br/>(News, SocialContent, Sources, Tokens)")]
        VERTEX["Vertex AI<br/>(Gemini 2.5 Flash Lite)"]
        GCS["Cloud Storage<br/>(Signed Image URLs)"]
        TASKS["Cloud Tasks Queue<br/>(Delayed /publish/{id} Callbacks)"]
    end

    subgraph External["External Networks & Feeds"]
        RSS_FEEDS["Target RSS Feeds"]
        LI_API["LinkedIn REST API v2"]
        TW_API["Twitter / X API v2"]
    end

    %% Interactions
    UI -->|Bearer JWT| ROUTING
    FB_AUTH -.->|ID Token| UI
    CRON -->|Daily 03:00 Trigger| ROUTING
    CONF -->|Retrieve Credentials| SECRETS
    ROUTING -->|Scrape & Parse| RSS_FEEDS
    ROUTING -->|Generate Copy| VERTEX
    ROUTING -->|Generate Signed URLs| GCS
    ROUTING -->|Persist Entities| DATASTORE
    ROUTING -->|Enqueue Task| TASKS
    TASKS -->|POST /publish/id (OIDC)| ROUTING
    ROUTING -->|Publish Posts & Assets| LI_API
    ROUTING -->|Publish Tweets| TW_API
```

---

## 🧩 Primary Subsystems & Directory Breakdown

### 1. [`appengine/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/README.md) &mdash; Deployment & Infrastructure
Defines deployment manifests, scheduling, and database indexing for Google Cloud App Engine:
- [`app.yaml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/app.yaml): Configures Java 21 runtime, standard `F1` instance tier, automatic scaling (max 2 instances), and HTTPS enforcement.
- [`cron.yaml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/cron.yaml): Schedules daily automated news feed synchronization (`03:00 Europe/Zurich`) on the `backend` service.
- [`dispatch.yaml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/dispatch.yaml): Directs domain traffic (`planner.catharsis.computer`, cron routes) to the `backend` service module.
- [`index.yaml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/index.yaml): Defines composite indexes in Cloud Datastore for multi-property sorting on RSS news, scheduled posts, and reading lists.

---

### 2. [`kotlin/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/README.md) &mdash; Core Application Logic
Contains the complete Kotlin backend implementation under `com.catharsis.ai4media.ai4mediaserver`:
- **Core Server**:
  - [`Application.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Application.kt): Initializes Firebase Admin SDK, installs Ktor plugins (HTTP Compression, Content Negotiation, Encrypted Cookie Sessions, Authentication), and registers route handlers.
  - [`CloudTasks.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/CloudTasks.kt): Manages delayed HTTP task scheduling on Google Cloud Tasks using OIDC service account authentication.
  - [`SocialContent.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt): Defines domain models ([`SocialContent`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L52), [`SocialContentRequest`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L42)), status enums ([`PostStatus`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L68)), network types ([`SocialNetwork`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/SocialContent.kt#L87)), and custom serializers.
- **Subpackages**:
  - [`Auth/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/README.md): Implements OAuth 2.0 PKCE generation ([`oAuthPKCE`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/oAuthPKCE.kt#L9)), token Datastore persistence with proactive refresh ([`TokenService`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenService.kt#L35)), and Cloud Tasks OIDC token validation ([`TokenVerifier`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenVerifier.kt#L10)).
  - [`Config/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/README.md): Merges environment variables with Google Cloud Secret Manager payloads ([`AppConfig`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/AppConfig.kt#L16), [`SecretManager`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/SecretManager.kt#L13)).
  - [`Networks/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/README.md): Integrates with third-party social APIs for automated posting ([`LinkedinConnector`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/LinkedinConnector.kt#L21) for UGC posts, image uploads, and first-comment links; [`TwitterConnector`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/TwitterConnector.kt#L13) for tweet publishing).
  - [`Routing/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/README.md): REST controllers covering post scheduling & dispatch ([`Routing.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/Routing.kt)), Gemini AI generation ([`AiGenerateRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AiGenerateRoutes.kt)), OAuth flows ([`AuthRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AuthRoutes.kt)), concurrent RSS syncing ([`NewsRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/NewsRoutes.kt)), reading lists ([`ReadingListRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ReadingListRoutes.kt)), queue inspection ([`ScheduledContentRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ScheduledContentRoutes.kt)), and RSS source management ([`SourceRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/SourceRoutes.kt)).
  - [`Utils/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/README.md): Core services including Vertex AI Gemini client ([`VertexAI.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt)), Datastore repositories ([`DataStore.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DataStore.kt), [`DatastoreSourceRepository.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DatastoreSourceRepository.kt)), Cloud Storage signed URL generator ([`CloudStorage.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/CloudStorage.kt)), and web/image scraping engines ([`NewsScrapper.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/NewsScrapper.kt), [`WebScrapper.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/WebScrapper.kt)).

---

### 3. [`resources/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/README.md) &mdash; Configuration & Static Web Frontend
Contains runtime server configurations and the bundled frontend Single Page Application (SPA):
- **Server Resources**:
  - [`application.conf`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/application.conf): HOCON configuration binding Ktor to port `8080` and specifying the application entry module.
  - [`logback.xml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/logback.xml): Logging configuration supporting local console logs and structured JSON logs for Google Cloud Logging.
- **Embedded Web Frontend ([`static/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/README.md))**:
  - [`index.html`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/index.html): Responsive SPA containing views for Google login, RSS curation, AI/Manual post composer, publishing queues, reading list, analytics charts, and settings.
  - [`app.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/app.js): Alpine.js reactive component, authenticated API wrapper, bilingual localization (EN/ES), and Chart.js visualizations.
  - [`auth.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/auth.js) & [`firebase-config.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/firebase-config.js): Firebase Authentication handler for Google Sign-In and session token forwarding.
  - [`styles.css`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/styles.css): Custom CSS utilities, scrollbar styling, and dynamic tessellation background patterns.

---

## 📖 Submodule Documentation Index

Detailed documentation for each specific module and subfolder is available at:

| Component | Path | Documentation Link |
| :--- | :--- | :--- |
| **App Engine Deployment** | `src/main/appengine` | [App Engine README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/appengine/README.md) |
| **Backend Core Package** | `src/main/kotlin/.../ai4mediaserver` | [Backend Package README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/README.md) |
| **Authentication & Tokens** | `src/main/kotlin/.../Auth` | [Auth Submodule README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/README.md) |
| **Configuration & Secrets** | `src/main/kotlin/.../Config` | [Config Submodule README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Config/README.md) |
| **Social Networks** | `src/main/kotlin/.../Networks` | [Networks Submodule README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Networks/README.md) |
| **HTTP Routing & APIs** | `src/main/kotlin/.../Routing` | [Routing Submodule README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/README.md) |
| **Utilities & Integrations** | `src/main/kotlin/.../Utils` | [Utils Submodule README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/README.md) |
| **Runtime Resources** | `src/main/resources` | [Resources README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/README.md) |
| **Web Frontend (SPA)** | `src/main/resources/static` | [Static Frontend README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/README.md) |

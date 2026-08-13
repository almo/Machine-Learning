 ## 1. What the System is Trying to Accomplish

  AI4MediaServer is an automated, serverless content aggregation, generative AI copywriting, and social media scheduling platform deployed on
  Google Cloud App Engine Standard (Java 21 / Kotlin / Ktor).
  The core objectives of the system are:
  1. Automated Content Ingestion: Continuously fetch, parse, deduplicate, and curate news articles from user-configured RSS feeds on a daily
  automated schedule.
  2. Generative AI Copywriting: Scrape article content and generate multi-persona, audience-tailored copy using Google Cloud Vertex AI (Gemini
  2.5 Flash Lite) with strict prompt-injection defenses:
      • LinkedIn Company Page: Professional industry curator persona.
      • LinkedIn Personal Profile: Thought leadership bump/discussion prompt designed to reshare and spark engagement.
      • Twitter / X: Punchy, high-engagement tweets adhering to the 280-character limit.
  3. Smart Algorithmic Scheduling: Automatically distribute posts into engagement "sweet spots" (Morning, Lunch, Commute, Night) while
  enforcing daily per-network quota limits.
  4. Reliable Cloud Tasks Dispatch: Queue delayed publication webhooks through Google Cloud Tasks protected by OpenID Connect (OIDC) service-
  account validation.
  5. Multi-Platform Publishing & Token Lifecycle: Publish rich posts and digital media assets to LinkedIn REST API v2 and Twitter/X API v2 with
  transparent, preemptive OAuth token refresh.
  6. Embedded Web Single-Page Application (SPA): Provide a lightweight, reactive UI (app.js + Tailwind CSS + Firebase Auth) for news curation,
  drafting, queue management, reading lists, and analytics.
  ──────
  ## 2. System Architecture & Component Inventory
  ```mermaid
flowchart TD
    subgraph Client["Frontend SPA (Alpine.js)"]
        UI["Web App (index.html / app.js)"]
        FB_AUTH["Firebase Authentication"]
    end

    subgraph Server["AI4MediaServer (Ktor / Java 21)"]
        KTOR["Ktor CIO Engine (Application.kt)"]
        ROUTING["Routing Controllers (Routing/)"]
        AUTH_MOD["Auth & Token Service (Auth/)"]
        SCRAPER["Scrapers (ROME / Jsoup / Skrape)"]
    end

    subgraph GCP["Google Cloud Platform"]
        SECRETS["Secret Manager (AI4MEDIA Secret)"]
        DATASTORE[("Cloud Datastore")]
        VERTEX["Vertex AI (Gemini 2.5 Flash Lite)"]
        GCS["Cloud Storage (Signed URLs)"]
        TASKS["Cloud Tasks (Delayed Queue)"]
        CRON["App Engine Cron (/internal/cron/news-sync)"]
    end

    subgraph External["External Networks & Feeds"]
        RSS_FEEDS["Target RSS Feeds"]
        LI_API["LinkedIn REST API v2"]
        TW_API["Twitter / X API v2"]
    end

    UI -->|Bearer JWT| ROUTING
    FB_AUTH -.->|ID Token| UI
    CRON -->|Daily 03:00 Sync| ROUTING
    KTOR -->|Load Secrets| SECRETS
    ROUTING -->|Ingest Feeds| RSS_FEEDS
    ROUTING -->|Scrape Web & Media| SCRAPER
    ROUTING -->|Synthesize Copy| VERTEX
    ROUTING -->|Sign Preview Images| GCS
    ROUTING -->|CRUD Entities| DATASTORE
    ROUTING -->|Enqueue Delayed Task| TASKS
    TASKS -->|"POST /publish/{id} (OIDC)"| ROUTING
    ROUTING -->|Preemptive Token Check| AUTH_MOD
    AUTH_MOD -->|Refresh Expired Tokens| LI_API
    AUTH_MOD -->|Refresh Expired Tokens| TW_API
    ROUTING -->|Publish UGC & Media| LI_API
    ROUTING -->|Publish Tweets| TW_API
```

### Submodule Documentation & Code References

   Subsystem                 | Documentation Link | Key Source Files
  ---------------------------|--------------------|--------------------------------------------------------------------------------------------
   Core Server Bootstrap     | README.md          | Application.kt, SocialContent.kt, CloudTasks.kt
   Authentication & Tokens   | README.md          | oAuthPKCE.kt, TokenService.kt, TokenVerifier.kt
   Configuration & Secrets   | README.md          | AppConfig.kt, SecretManager.kt
   Social Media Connectors   | README.md          | LinkedinConnector.kt, TwitterConnector.kt
   HTTP Routing & REST APIs  | README.md          | Routing.kt, AiGenerateRoutes.kt, NewsRoutes.kt, AuthRoutes.kt, ScheduledContentRoutes.kt,
                             |                    | ReadingListRoutes.kt, SourceRoutes.kt
   Utilities & Integrations  | README.md          | VertexAI.kt, DataStore.kt, DatastoreSourceRepository.kt, NewsScrapper.kt, WebScrapper.kt,
                             |                    | CloudStorage.kt
   App Engine Infrastructure | README.md          | app.yaml, cron.yaml, dispatch.yaml, index.yaml
   Client-Side Web SPA       | README.md          | index.html, app.js, auth.js, firebase-config.js, styles.css
  ──────
  
## 3. How the System Works Across the Lifecycle

### Phase 1: Configuration, Startup, and Security

* **Dynamic Secret Loading:** On startup, `AppConfig.kt:16` combines environment variables with runtime secrets loaded from Google Cloud Secret Manager via `SecretManager.kt:13`.
* **Engine Middleware in `Application.kt:35`:**
  * Zstd / Gzip / Deflate Compression with BREACH attack mitigation.
  * Encrypted Cookie Sessions (`AI4MEDIA_SESSION`) using AES encryption and HMAC-SHA256 signing.
  * **Authentication Schemes:**
    * `firebase-auth`: Validates user Firebase ID tokens from the frontend.
    * `google-cloud-tasks`: Validates Google OIDC JWT tokens on automated callbacks via `TokenVerifier.kt:10`.
    * `auth-twitter` & `auth-linkedin`: OAuth 2.0 PKCE authorization handlers using `oAuthPKCE.kt:9`.

### Phase 2: RSS Ingestion & Heuristic Web Scraping

* **Cron Trigger:** App Engine Cron invokes `GET /internal/cron/news-sync` daily at 03:00 Europe/Zurich (`cron.yaml`).
* **Concurrent Ingestion:** `NewsRoutes.kt` dispatches work across 20 coroutine workers, parsing feeds with ROME tools (`SyndFeedInput`) and deduplicating items with SHA-256 URL keys in Datastore.
* **Media Resolution:** `NewsScrapper.kt` and `WebScrapper.kt` extract preview images via a hierarchical fallback chain: OpenGraph (`og:image`) → Twitter Cards (`twitter:image`) → JSON-LD structured data → Apple touch icons → DOM image dimensions.
* **Data Pruning:** Articles older than `AppConfig.rssNewsRetentionDays` (default: 30 days) are automatically purged.

### Phase 3: Generative AI Copywriting Pipeline

* **Endpoint `POST /api/ai/generate`:** Located in `AiGenerateRoutes.kt:86`.
* **Scraping:** Fetches up to 5,000 characters of clean article text via Jsoup.
* **Prompt Synthesis & Defenses:** Constructs structured instructions for `gemini-2.5-flash-lite` wrapped inside security boundary tags (`<article_text>`, `<url>`) to defend against prompt injection.
* **Structured Response:** `VertexAI.kt:43` returns a typed JSON payload containing:
  * `linkedinCompany`: Professional industry summary.
  * `linkedinBump`: Provocative personal engagement thought.
  * `twitter`: 280-character limited post.
  * `strategyRationale` & SEO tags.

### Phase 4: Smart Scheduling & Cloud Tasks Queue

* **Endpoint `POST /schedule`:** Located in `Routing.kt:130`.
* **Three Scheduling Modes:**
  1. `NOW`: Immediately launches an asynchronous publishing coroutine.
  2. `SCHEDULED`: Sets a specific ISO-8601 timestamp.
  3. `AUTOMATIC`: Calls `Routing.kt:63` to allocate posts into available daily sweet spots (Morning, Lunch, Commute, Night) without exceeding per-network limits (Twitter: 5/day, LinkedIn: 2/day).
* **Task Enqueuing:** For future posts, `CloudTasks.kt:18` creates an HTTP callback targeting `/publish/{id}` with an OIDC identity token and execution timestamp. The post entity is recorded in Datastore under `SocialContent.kt:73` or `SocialContent.kt:72`.

### Phase 5: Multi-Platform Publishing & Token Refresh

* **Webhook Callback `POST /publish/{id}`:** Triggered by Google Cloud Tasks or manual override in `Routing.kt:241`.
* **Preemptive Token Verification:** `TokenService.kt:55` inspects token expiration against a 60-second safety window. If expiring or expired, it automatically executes a refresh grant with LinkedIn or Twitter and updates Datastore.
* **Publishing Execution:**
  * **LinkedIn (`LinkedinConnector.kt:26`):** Registers and uploads image binaries to the LinkedIn Digital Media Asset service, creates a UGC organization post, and posts the source URL as a first comment to maximize algorithm reach.
  * **Twitter/X (`TwitterConnector.kt:14`):** Publishes the formatted tweet via Twitter API v2.
* **State Finalization:** Datastore entity is updated with `SocialContent.kt:74` and the external `targetUrn` / `tweetId`, or marked as `SocialContent.kt:75` on error.

### Phase 6: Single-Page Application (SPA) Experience

* Embedded in `README.md` and served directly by Ktor.
* Uses `app.js` for reactive views (`login`, `rss`, `compose`, `scheduled`, `reading_list`, `stats`, `settings`).
* Includes real-time bilingual localization (English and Spanish), Firebase Google Sign-In, and interactive `app.js:542` radar and activity charts.

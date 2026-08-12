# Routing Module

This directory contains the routing layer and endpoint controllers for the **AI4MediaServer** backend, built using the [Ktor](https://ktor.io/) framework.

## Purpose

The `Routing` package is responsible for defining the HTTP API surface of the application. It handles:
- **Authentication & Authorization**: Integrating Firebase Auth for user requests, OAuth 2.0 flows for social platforms (LinkedIn, Twitter/X), and Google Cloud Tasks OIDC token validation.
- **AI Copy Generation**: Transforming web articles into tailored social media posts via Google Gemini.
- **Content Scheduling & Publishing**: Managing immediate, scheduled, and algorithmic "sweet-spot" auto-scheduling with Google Cloud Tasks and social connectors.
- **RSS Feed Ingestion & Curation**: Aggregating, parsing, deduplicating, and managing RSS feeds and personal reading lists.
- **Source Management**: CRUD operations for user RSS subscription feeds.

---

## File Enumeration & Details

| File | Primary Role & Purpose | Key Endpoints & Features |
| :--- | :--- | :--- |
| [`Routing.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/Routing.kt) | Core routing configuration, post scheduler, and Cloud Tasks executor | `POST /schedule`<br>`POST /publish/{id}`<br>Static content hosting |
| [`AiGenerateRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AiGenerateRoutes.kt) | AI-powered social media copy generation from scraped URLs | `POST /api/ai/generate` |
| [`AuthRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AuthRoutes.kt) | OAuth 2.0 authentication flows and connection status checks | `GET /api/auth/init-twitter`<br>`GET /api/auth/init-linkedin`<br>`GET /api/auth/status`<br>`GET /auth/twitter/callback`<br>`GET /auth/linkedin/callback` |
| [`NewsRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/NewsRoutes.kt) | High-concurrency RSS feed fetching, deduplication, and news reader APIs | `GET /internal/cron/news-sync`<br>`GET /api/news`<br>`POST /api/news/sync`<br>`PUT /api/news/read` |
| [`ReadingListRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ReadingListRoutes.kt) | Personal article bookmarking and reading list management | `GET /api/reading-list`<br>`POST /api/reading-list`<br>`PUT /api/reading-list/{id}`<br>`DELETE /api/reading-list/{id}` |
| [`ScheduledContentRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ScheduledContentRoutes.kt) | Queue tracking, publishing history, and post lifecycle management | `GET /api/scheduled`<br>`GET /api/published`<br>`DELETE /api/scheduled/{id}` |
| [`SourceRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/SourceRoutes.kt) | RSS feed source configuration and management | `GET /api/sources`<br>`POST /api/sources`<br>`GET /api/sources/{id}`<br>`PUT /api/sources/{id}`<br>`DELETE /api/sources/{id}` |

---

### Detailed Breakdown

### 1. [`Routing.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/Routing.kt)
- **Primary Function**: Serves as the central application route registrar and orchestrates post scheduling and publishing.
- **Key Components**:
  - `Application.configureRouting()`: Main routing setup serving static files and protected scheduling endpoints.
  - `calculateOptimizedScheduleTime()`: Implements smart auto-scheduling algorithm by checking daily limits per social network (`TWITTER`, `LINKEDIN`) and assigning posts into optimal engagement "sweet spots" (Morning, Lunch, Commute, Night).
  - `UserSettingsRegistry`: Manages user-specific scheduling limits and time windows.
  - `POST /schedule`: Handles immediate publication (`NOW`), manual scheduled time, or algorithmic distribution (`AUTOMATIC`) via Google Cloud Tasks.
  - `POST /publish/{id}`: Secure task callback endpoint triggered by Cloud Tasks or manual execution to dispatch posts to social media platform APIs via connectors and update Datastore entity statuses (`PUBLISHED`, `FAILED`, `PUBLISHING`).

### 2. [`AiGenerateRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AiGenerateRoutes.kt)
- **Primary Function**: Extracts web article content and utilizes Gemini LLM to generate audience-optimized social media copy.
- **Key Components**:
  - `POST /api/ai/generate`: Protected endpoint taking an input article URL.
  - `buildAiGeneratedPrompt()`: Constructs structured prompt with prompt-injection defense mechanisms and enforces JSON output schema.
  - **Scraping**: Fetches target page HTML and cleans text using `Jsoup`.
  - **Generation**: Interacts with `GeminiClient` to return tailored copy for:
    - LinkedIn Company Page (formal industry curator persona).
    - LinkedIn Personal Thought Leadership bump (provocative question/engagement prompt).
    - Twitter/X post (punchy copy fitting within character constraints).
    - SEO hashtags and content strategy rationale.

### 3. [`AuthRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/AuthRoutes.kt)
- **Primary Function**: Manages third-party OAuth 2.0 connections for publishing channels.
- **Key Components**:
  - `GET /api/auth/init-twitter` & `GET /api/auth/init-linkedin`: Generates and authorizes user session before browser redirect.
  - `GET /auth/twitter/callback` & `GET /auth/linkedin/callback`: Intercepts OAuth callbacks, extracts tokens, and persists credentials using `TokenService`.
  - `GET /api/auth/status`: Reports connection state (valid active tokens) for Twitter and LinkedIn per user.

### 4. [`NewsRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/NewsRoutes.kt)
- **Primary Function**: Coordinates large-scale RSS feed synchronization, article indexing, and reading statuses.
- **Key Components**:
  - `GET /internal/cron/news-sync`: App Engine cron job trigger protected by `X-Appengine-Cron` header validation.
  - `syncNewsForAllSources()` & `syncNewsForUser()`: Concurrent feed fetcher utilizing Kotlin Coroutines, Channels, and a 20-worker consumer pool.
  - `processSourceAndSaveNews()`: Fetches XML feeds via `HttpClient`, parses with `SyndFeedInput` (ROME), strips HTML, generates category fallback images or Google Cloud Storage signed URLs (`ImageUrlSignerService`), and deduplicates entries using SHA-256 hashed URLs as Datastore keys.
  - `deleteOldRSSNews()`: Automatically purges news items older than configured retention limits (`AppConfig.rssNewsRetentionDays`).
  - `GET /api/news`: Retrieves cached news with optional unread filters.
  - `PUT /api/news/read`: Batch updates read/unread status for news entries.

### 5. [`ReadingListRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ReadingListRoutes.kt)
- **Primary Function**: Provides CRUD operations for user-curated article bookmarks and reading list entries.
- **Key Components**:
  - `GET /api/reading-list`: Lists all saved articles for the authenticated user, ordered by creation date.
  - `POST /api/reading-list`: Adds an article with optional notes/comments and publication date.
  - `PUT /api/reading-list/{id}`: Updates existing item metadata or comments.
  - `DELETE /api/reading-list/{id}`: Removes an item from the reading list.

### 6. [`ScheduledContentRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/ScheduledContentRoutes.kt)
- **Primary Function**: Manages the post queue, history log, and cancellation of queued publications.
- **Key Components**:
  - `GET /api/scheduled`: Retrieves pending upcoming posts with status `SCHEDULED` or `AUTOSCHEDULED`.
  - `GET /api/published`: Queries historical post logs (`PUBLISHED` or `FAILED`) over a configurable timeframe (default 30 days).
  - `DELETE /api/scheduled/{id}`: Performs soft-deletion by updating post status to `DELETED`.

### 7. [`SourceRoutes.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Routing/SourceRoutes.kt)
- **Primary Function**: Exposes REST endpoints to manage user-subscribed RSS feeds and content sources.
- **Key Components**:
  - `route("/api/sources")`: CRUD interface delegating to `DatastoreSourceRepository`.
  - Enforces strict user ownership verification on single-source queries, updates, and deletions.

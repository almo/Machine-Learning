# AI4MediaServer Utilities & Integration Services

This directory (`com.catharsis.ai4media.ai4mediaserver.Utils`) provides the core utility layer, external cloud service integrations, scraping pipelines, and persistence helpers for the **AI4Media** backend server.

---

## 🎯 Directory Purpose

The modules in this directory serve four primary architectural functions:
1. **Generative AI & LLM Processing**: Interfacing with Google Cloud Vertex AI (Gemini) to generate and format social media content while tracking token consumption and cost metrics.
2. **Cloud Storage & Asset Delivery**: Interfacing with Google Cloud Storage (GCS) to generate secure V4 signed URLs for media and preview images.
3. **Data Persistence & Repositories**: Providing wrapper services and CRUD repository abstractions for Google Cloud Datastore entities (`SocialContent`, `RSSFeedSource`).
4. **Web & Media Scraping**: Extracting clean article text, metadata, OpenGraph tags, and preview imagery from web sources using multi-tier heuristic extraction.

---

## 📁 File Index & Component Breakdown

| File | Primary Symbols | Description & Responsibilities |
| :--- | :--- | :--- |
| [`CloudStorage.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/CloudStorage.kt) | [`ImageUrlSignerService`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/CloudStorage.kt#L13) | Asynchronously generates Google Cloud Storage V4 signed URLs for image resources. Parses `gs://` and custom URI formats, dispatches signing operations onto `Dispatchers.IO`, and handles credential validation for Service Accounts vs User Credentials. |
| [`DataStore.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DataStore.kt) | [`DataStoreWrapper`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DataStore.kt#L21), [`ClaimPublishResult`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DataStore.kt#L14) | Google Cloud Datastore helper for social content workflows. Manages saving `SocialContent` records, atomic 90-second lease-locked claiming (`claimPostForPublishing`), optimistic fencing completion (`completePublishing`), and querying upcoming auto-scheduled posts. |
| [`DatastoreSourceRepository.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DatastoreSourceRepository.kt) | [`DatastoreSourceRepository`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/DatastoreSourceRepository.kt#L15) | Repository layer for managing RSS and news feed sources (`RSSFeedSource` kind) in Datastore. Implements complete CRUD operations (`create`, `findByUserId`, `findById`, `update`, `delete`) and entity-to-model mapping with date/time parsing. |
| [`NewsScrapper.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/NewsScrapper.kt) | [`NewsScraper`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/NewsScrapper.kt#L11), [`ScrapedContent`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/NewsScrapper.kt#L71) | Web and article scraping engine built on `skrape{it}`. Extracts article titles, OpenGraph and meta descriptions, primary content body text, and OpenGraph preview images. |
| [`SourceDTOs.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/SourceDTOs.kt) | [`Source`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/SourceDTOs.kt#L26), [`SourceRequest`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/SourceDTOs.kt#L39), [`ZonedDateTimeSerializer`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/SourceDTOs.kt#L13) | Domain data models and Data Transfer Objects (DTOs) for feed source management, along with a custom `kotlinx.serialization` serializer for ISO-8601 `ZonedDateTime`. |
| [`VertexAI.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt) | [`GeminiClient`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt#L43), [`VertexAiConfig`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt#L12), [`PricingConfig`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt#L18), [`TokenMetrics`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/VertexAI.kt#L23) | Google Cloud Vertex AI client wrapping Gemini (`gemini-2.5-flash` by default, configurable via `VERTEX_AI_MODEL`). Configures generation parameters (structured JSON output, temperature), non-blocking asynchronous generation via `Dispatchers.IO`, and token/cost analytics. |
| [`WebScrapper.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/WebScrapper.kt) | [`ImageResolver`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Utils/WebScrapper.kt#L5) | Jsoup-powered fallback image extraction pipeline. Evaluates a ranked chain of heuristics: OpenGraph (`og:image`) &rarr; Twitter Cards (`twitter:image`) &rarr; JSON-LD structured data &rarr; High-resolution Apple touch/favicon icons &rarr; DOM body container image dimensions. |

---

## 🔄 End-to-End Workflow Context

These utilities work in concert within the AI4Media pipeline:

```
[Web / RSS Feed] 
       │
       ▼ (NewsScrapper.kt / WebScrapper.kt)
[Article Content & Extracted Images]
       │
       ▼ (VertexAI.kt - GeminiClient)
[AI Generated Social Posts & Copy]
       │
       ▼ (DataStore.kt & DatastoreSourceRepository.kt)
[Google Cloud Datastore Entities]
       │
       ▼ (CloudStorage.kt - ImageUrlSignerService)
[Signed Media URLs & Delivery]
```

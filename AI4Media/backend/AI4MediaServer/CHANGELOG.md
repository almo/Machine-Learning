# Changelog

All notable changes to the **AI4MediaServer** backend and UI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v1.1.0] - 2026-08-16

### 🚀 Google Cloud Release
* **App Engine Version ID**: `v1-1-0-f3a89e5`
* **Target Service**: `backend` (Standard Environment)
* **Instance Class**: `F2` (768 MB RAM / 1.2 GHz CPU)
* **Runtime**: Java 21 OpenJDK / Ktor 3.4.1 (CIO Engine)
* **Base URL**: `https://planner.catharsis.computer`
* **Commit**: [`f3a89e5`](https://github.com/almo/Machine-Learning/commit/f3a89e5)

### ✨ Features & Enhancements
- **Auto-Scheduling Preferences & Datastore Persistence** ([#38](https://github.com/almo/Machine-Learning/issues/38)):
  - Persisted user publishing quotas and sweet-spot time windows in Google Cloud Datastore (`UserSettings` kind).
  - Added in-memory `ConcurrentHashMap` caching to `UserSettingsRegistry` for zero-latency lookups.
  - Implemented `GET /api/settings/schedule` and `PUT /api/settings/schedule` REST endpoints.
  - Built an interactive, reactive **Auto-Scheduling Preferences** card in the UI (`app.js` & `index.html`) with bilingual localization.
- **Vertex AI Model Modernization** ([#49](https://github.com/almo/Machine-Learning/issues/49)):
  - Upgraded generative model to **`gemini-2.5-flash`** with dynamic `VERTEX_AI_MODEL` environment variable support and temperature calibrated to `0.3f`.

### 🐛 Bug Fixes & Reliability
- **Ghost Publishing Prevention** ([#32](https://github.com/almo/Machine-Learning/issues/32)):
  - Tracked and persisted Cloud Task resource names in Datastore.
  - Added proactive task deletion on soft-delete (`DELETE /api/scheduled/{id}`) and state validation in `/publish/{id}` to skip deleted/failed posts.
- **Multi-Tenant RSS Collisions** ([#33](https://github.com/almo/Machine-Learning/issues/33)):
  - Scoped `RSSNews` Datastore keys to `${userId}_${urlHash}` to prevent cross-tenant overwrites.
- **OAuth Callback 404 & Expiration** ([#37](https://github.com/almo/Machine-Learning/issues/37)):
  - Corrected OAuth callback redirects to `/?oauth=success` and extended session cookie TTL to 600s.
- **Orphan File Package Declarations** ([#46](https://github.com/almo/Machine-Learning/issues/46)):
  - Restored package declarations to `CloudStorage.kt` and `WebScrapper.kt`.

### ⚙️ Infrastructure & Performance
- **App Engine F2 & JVM GC Optimization** ([#47](https://github.com/almo/Machine-Learning/issues/47)):
  - Upgraded instance tier to `F2` (768 MB) and configured JVM flags `-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC` to prevent Out-Of-Memory container kills.
- **HTTP Client Connection Leak** ([#51](https://github.com/almo/Machine-Learning/issues/51)):
  - Consolidated OAuth provider lookups to reuse a shared singleton `HttpClient(CIO)` with graceful shutdown lifecycle hooks.
- **Logback Configuration Cleanup** ([#52](https://github.com/almo/Machine-Learning/issues/52)):
  - Eliminated conflicting duplicate `<root>` appenders in `logback.xml` via dynamic `${LOG_APPENDER:-STDOUT}` routing.

---

## [v1.0.0] - 2026-08-15

### 🚀 Google Cloud Release
* **App Engine Version ID**: `main100` (Initial baseline)
* **Commit**: [`038fdad`](https://github.com/almo/Machine-Learning/commit/038fdad)

### 📦 Initial Release
* Initial baseline implementation of AI4MediaServer:
  - Ktor CIO backend with Firebase Authentication and OAuth 2.0 PKCE for Twitter and LinkedIn.
  - RSS feed ingestion, ROME XML feed parsing, and Skrape{it} web content scraping.
  - Generative AI post drafting via Google Cloud Vertex AI.
  - Google Cloud Tasks background queue dispatch and Cloud Storage image signing.
  - Embedded Alpine.js Single Page Application dashboard.

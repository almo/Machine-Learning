# AI4Media Server Resources (`src/main/resources`)

This directory contains the runtime configurations, logging definitions, and static client-side web assets for the **AI4MediaServer** backend application (a Kotlin/Ktor service deployed on Google Cloud App Engine).

---

## 📁 Directory Structure

```text
src/main/resources/
├── application.conf    # Ktor HOCON application & deployment configuration
├── logback.xml         # Logback logging framework configuration (Console & GCP JSON)
├── README.md           # This documentation file
└── static/             # Client-side Single Page Application (SPA) resources
    ├── app.js          # Core Alpine.js reactive application logic & API client
    ├── auth.js         # Firebase Auth integration & session management
    ├── favicon.ico     # Web application favicon
    ├── firebase-config.js # Firebase SDK credentials and configuration
    ├── index.html      # Main HTML5 layout and view templates
    ├── README.md       # Comprehensive documentation for the frontend web interface
    └── styles.css      # Custom styling, scrollbars, and background tessellations
```

---

## 🛠️ Components & File Inventory

### 1. [`application.conf`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/application.conf)
- **Format:** HOCON (Human-Optimized Config Object Notation)
- **Role:** Main configuration file consumed by the Ktor engine (`io.ktor.server.cio.EngineMain`).
- **Configuration Details:**
  - **Deployment Port:** Configured to bind on port `8080` (`ktor.deployment.port = 8080`).
  - **Application Module:** Registers the application bootstrap module `com.catharsis.ai4media.ai4mediaserver.ApplicationKt.module` to initialize routing, serialization, authentication, and background services.

### 2. [`logback.xml`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/logback.xml)
- **Format:** XML (Logback configuration)
- **Role:** Controls application logging format, log levels, and appenders across environments (local development and Google Cloud Platform).
- **Key Features:**
  - **`STDOUT` Appender:** Standard formatted console logs (`%d{...} [%thread] %-5level %logger{36} - %msg%n`) for local debugging at `TRACE` level.
  - **`GCP_JSON_CONSOLE` Appender:** Structured JSON log layout using Jackson (`ch.qos.logback.contrib.json.classic.JsonLayout` with ISO-8601 timestamps), enabling rich log parsing in Google Cloud Logging / Cloud Run / App Engine at `INFO` level.
  - **Noise Suppression:** Caps verbose framework loggers (`org.eclipse.jetty`, `io.netty`) at `INFO` level.

### 3. [`static/`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static) (Web Frontend Assets)
- **Role:** Embedded client-side Single Page Application (SPA) served statically by Ktor.
- **Overview:** Provides an intuitive web UI for AI-powered social media generation, RSS curation, automated scheduling (LinkedIn, Twitter/X), analytics tracking, and reading list management.
- **Key Sub-Assets:**
  - [`index.html`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/index.html): HTML5 layout containing reactive Alpine.js views for authentication, news feed, post composer, scheduler queue, analytics, and settings.
  - [`app.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/app.js): Alpine.js state controller (`app()`), REST API integration, i18n localization (EN/ES), and Chart.js rendering.
  - [`auth.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/auth.js) & [`firebase-config.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/firebase-config.js): Firebase Authentication client with Google OAuth pop-up login and JWT token propagation.
  - [`styles.css`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/styles.css): Global UI tweaks, Alpine cloak handling, and geometric background patterns.
- *For in-depth details on the frontend architecture and API routes, refer to the [Static Frontend README](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/README.md).*

---

## 🚀 Runtime & Packaging Context

When building and packaging `AI4MediaServer` (via Gradle Shadow JAR):
1. **Resource Bundling:** All files in this directory are packaged at the root of the executable JAR.
2. **Ktor Static Routing:** Ktor serves the contents of the `static/` directory to root web requests (`/` and static sub-paths).
3. **App Engine Deployment:** `application.conf` and `logback.xml` ensure seamless execution and structured log ingestion on Google App Engine Java 21 runtime.

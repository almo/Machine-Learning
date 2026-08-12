# Google App Engine Configuration (`src/main/appengine`)

This directory contains the deployment descriptors, routing configurations, background job schedules, and database index definitions for deploying the **AI4MediaServer** backend to **Google Cloud App Engine (Standard Environment)**.

---

## Directory Overview

The `appengine` directory is the central location for Google Cloud App Engine configuration files used by the Gradle App Engine plugin (`com.google.cloud.tools.appengine-appyaml`) and the `gcloud` CLI. These files configure how the Ktor/JVM service is run, scaled, routed, scheduled, and indexed in Google Cloud Datastore / Firestore.

---

## Files and Descriptions

| File | Purpose | Key Details & Responsibilities |
| :--- | :--- | :--- |
| [`app.yaml`](./app.yaml) | **App Engine Service Descriptor** | Defines runtime environment, instance scaling, execution command, and handlers for the `backend` service. |
| [`cron.yaml`](./cron.yaml) | **Cron Job Schedule Configuration** | Configures scheduled background tasks managed by the Google Cloud App Engine Cron service. |
| [`dispatch.yaml`](./dispatch.yaml) | **URL Routing Rules** | Routes incoming HTTP requests to designated App Engine services based on domain and path patterns. |
| [`index.yaml`](./index.yaml) | **Datastore Composite Indexes** | Specifies composite indexes required by Google Cloud Datastore / Firestore for multi-property queries. |

---

### Detailed File Explanations

### 1. [`app.yaml`](./app.yaml)
The primary service configuration file for deploying the backend service on App Engine Standard.
- **Runtime**: `java21` (Java 21 runtime).
- **Service Name**: `backend` (deployed as a non-default microservice module).
- **Instance Class**: `F1` (standard micro instance tier).
- **Entrypoint**: `java -jar AI4MediaServer-all.jar` (runs the packaged Shadow fat JAR).
- **Scaling**: `automatic_scaling` with a cap of `max_instances: 2` to optimize resource usage and prevent unexpected scaling costs.
- **Handlers**:
  - `/favicon.ico` served statically from `static/favicon.ico`.
  - Catch-all `.*` routed automatically to the Ktor application with HTTPS enforced (`secure: always`).
- **Environment Variables**: Defines `APPENGINE_BASE_URL` (`https://planner.catharsis.computer`).

---

### 2. [`cron.yaml`](./cron.yaml)
Configures automated background tasks triggered by App Engine's Cron Service.
- **Scheduled Job**: Daily News Synchronization (`/internal/cron/news-sync`).
- **Schedule**: Triggers every day at `03:00` in the `Europe/Zurich` timezone.
- **Target**: Explicitly targets the `backend` service.

---

### 3. [`dispatch.yaml`](./dispatch.yaml)
Defines project-level traffic routing rules for directing incoming requests to the appropriate App Engine service (`backend` vs `default`).
- **Custom Domain Routing**: Routes `planner.catharsis.computer/*` directly to the `backend` service.
- **Cron Routing**: Explicitly routes all `*/internal/cron/*` endpoints to the `backend` service.
- **Direct App Engine Subdomain**: Maps `backend-dot-meta-gear-464720-g3.oa.r.appspot.com/*` to the `backend` service.
- **Catch-All**: Forwards all remaining unmatched traffic (`*/*`) to the `default` service.

---

### 4. [`index.yaml`](./index.yaml)
Specifies composite indexes required by Google Cloud Datastore (or Firestore in Datastore mode) for queries that filter and sort on multiple entity properties:
- **`RSSNews`**:
  - `userId` (ASC) + `publishedAt` (DESC): Queries user RSS feeds ordered chronologically by newest articles.
  - `read` (ASC) + `publishedAt` (DESC): Queries articles filtered by read status and sorted descending.
- **`SocialContent`**:
  - `network` + `status` + `userId` + `scheduledTime` (DESC): Fetches filtered scheduled posts ordered "Newest First".
  - `network` + `status` + `userId` + `scheduledTime` (ASC): Fetches filtered scheduled posts ordered "Oldest First".
  - `userId` + `scheduledTime` (ASC & DESC): General user social posts sorting.
- **`ReadingList`**:
  - `userId` (ASC) + `createdAt` (DESC): Retrieves reading list items for a user ordered by creation date descending.

---

## Deployment Reference

When deploying using Gradle (via `com.google.cloud.tools.appengine-appyaml`):
```bash
./gradlew appengineDeploy
```

Or deploying individual components with the Google Cloud CLI (`gcloud`):
```bash
# Deploy service application
gcloud app deploy src/main/appengine/app.yaml

# Deploy cron schedules
gcloud app deploy src/main/appengine/cron.yaml

# Deploy routing dispatch rules
gcloud app deploy src/main/appengine/dispatch.yaml

# Deploy Datastore indexes
gcloud app deploy src/main/appengine/index.yaml
```

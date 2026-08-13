# AI4Media Frontend (Static Resources)

This directory (`src/main/resources/static`) contains the client-side single-page application (SPA) for the **AI4Media Platform**. It is served statically by the underlying Kotlin/Ktor backend server (`AI4MediaServer`) and provides a comprehensive web interface for news curation, AI-driven content generation, multi-network social media scheduling (LinkedIn, Twitter/X), analytics tracking, and RSS source management.

---

## 🏗️ Architecture & Technology Stack

- **Reactivity & State Management:** [Alpine.js 3.x](https://alpinejs.dev/) (lightweight reactive component architecture via `app.js`)
- **Styling & Layout:** [Tailwind CSS](https://tailwindcss.com/) (utility-first styling via CDN) + Custom CSS (`styles.css`)
- **Authentication:** [Firebase Authentication](https://firebase.google.com/docs/auth) with Google Sign-In (`auth.js`, `firebase-config.js`)
- **Data Visualization:** [Chart.js](https://www.chartjs.org/) (interactive radar & time-series line charts)
- **Icons & Typography:** [Font Awesome 6](https://fontawesome.com/) & system typography
- **Internationalization (i18n):** Built-in real-time bilingual support (English & Spanish)
- **Backend Communication:** REST API communication with the Ktor backend using JSON payloads and Firebase Bearer authentication tokens.

---

## 📁 File Enumeration

| File | Type | Description |
| :--- | :--- | :--- |
| [`index.html`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/index.html) | HTML5 / Alpine Template | Main entry point and single-page layout containing all application views, navigation menus, modals, and CDN dependencies. |
| [`app.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/app.js) | JavaScript (ES6) | Core application controller and Alpine.js component (`app()`), containing all reactive state, API integration methods, translations, filters, and chart rendering logic. |
| [`auth.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/auth.js) | JavaScript (ES Module) | Firebase authentication wrapper. Handles Google OAuth pop-up login, user session monitoring, JWT token retrieval, and view dispatch events. |
| [`firebase-config.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/firebase-config.js) | JavaScript (ES Module) | Firebase client SDK initialization module containing project credentials and exporting auth services. |
| [`styles.css`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/styles.css) | CSS3 | Custom stylesheets for Alpine `[x-cloak]` flicker prevention, subtle scrollbar formatting, and randomized background tessellation patterns. |
| [`favicon.ico`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/favicon.ico) | Binary Icon | Application favicon displayed in the browser tab. |

---

## 🔍 Detailed Component & File Breakdown

### 1. [`index.html`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/index.html)
The primary HTML document serving as the single-page application container. Structured into reactive sections toggled by Alpine's `currentView` state:
- **Login View (`login`):** Displays a login card with Google authentication, language toggle, and dynamic tessellation background patterns.
- **News Feed View (`rss`):** Master-detail interface displaying categorized RSS articles, source filters, read/unread status badges, and quick actions to curate or save articles.
- **Composer View (`compose`):** Dual-mode authoring tool:
  - *AI Mode:* Accepts article URLs, queries AI generation endpoints, and provides editable post drafts for LinkedIn, Twitter, and bump/follow-up posts.
  - *Manual Mode:* Custom text composition with network selection (LinkedIn/Twitter) and flexible scheduling options (Now, Random/Automatic slot, Specific date/time).
- **Scheduled Queue View (`scheduled`):** Tabular publishing queue with regex tag filtering, date filtering, manual trigger ("Publish Now"), and post deletion.
- **Reading List View (`reading_list`):** Bookmark management interface for storing articles, personal comments, and processing dates.
- **Analytics & Stats View (`stats`):** Metrics overview cards, a Chart.js Radar Chart for tag frequency analysis, a 14-day Time-Series activity chart, and links to published social posts.
- **Settings View (`settings`):** Social media account connection statuses (OAuth links for Twitter & LinkedIn), RSS feed management (add/edit/delete sources and categories), and manual news synchronization triggers.
- **Modals:** Pop-up dialogs for creating/editing categories, RSS sources, and reading list entries.

### 2. [`app.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/app.js)
Defines the `app()` Alpine.js component that controls client state and behavior:
- **State Management:** Tracks active view (`currentView`), selected articles, categories, RSS sources, scheduled posts, published logs, and reading lists.
- **Internationalization (`i18n`):** Contains complete translation dictionaries for English (`en`) and Spanish (`es`) with a key lookup helper `t(key)`.
- **API Client (`apiCall`):** Centralized `fetch` wrapper that attaches Firebase ID tokens (`Authorization: Bearer <token>`) and handles JSON/text parsing and error propagation.
- **Curation & Generation Pipelines:** Manages the AI generation flow (`generateAiContent`, `scheduleAiPosts`, `scheduleSingleAiPost`) and manual submissions (`submitManualPost`).
- **Data Visualizations:** Instantiates and updates Chart.js instances (`updateRadarChart`, `updateTimeSeriesChart`) based on historical publication data.
- **Offline/Dev Fallbacks:** Provides graceful mock data fallbacks for news, sources, and AI generation if the backend is unreachable during frontend development.

### 3. [`auth.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/auth.js)
Integrates Firebase Authentication with Alpine.js:
- Binds to the Google sign-in button via `signInWithPopup`.
- Listens to authentication state changes with `onAuthStateChanged`.
- Dispatches window events (`auth-success`, `auth-logout`) to switch views in `index.html`.
- Exposes utility functions on `window`:
  - `window.firebaseSignOut()`: Signs out the active user.
  - `window.getFirebaseAuthToken()`: Asynchronously fetches a valid Firebase ID token (waiting for auth initialization).
  - `window.getFirebaseUserId()`: Returns the current user's UID.

### 4. [`firebase-config.js`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/firebase-config.js)
Initializes the Firebase Modular Web SDK (`firebase/app` and `firebase/auth`) using the project credentials (`meta-gear-464720-g3`) and exports `auth` and `provider` instances.

### 5. [`styles.css`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/resources/static/styles.css)
Contains global styles and cosmetic enhancements:
- `[x-cloak]`: Hides uncompiled Alpine.js templates until scripts have loaded.
- WebKit scrollbar styling for clean, minimalist scrolling.
- Six subtle geometric background pattern classes (`bg-pattern-1` to `bg-pattern-6`) randomized on the login screen.

---

## 🔗 Backend API Endpoints Interfaced

The frontend communicates with the following Ktor server endpoints:

| Endpoint | Method | Purpose |
| :--- | :--- | :--- |
| `/api/news` | `GET` | Retrieve RSS news articles (supports `?unreadOnly=true/false`). |
| `/api/news/read` | `PUT` | Batch update read/unread state of articles. |
| `/api/news/sync` | `POST` | Trigger background RSS synchronization on the server. |
| `/api/sources` | `GET`, `POST` | Fetch all RSS sources or register a new source. |
| `/api/sources/{id}` | `PUT`, `DELETE` | Update or delete an existing RSS source. |
| `/api/ai/generate` | `POST` | Generate social media posts from a source URL using AI. |
| `/schedule` | `POST` | Schedule one or multiple posts (LinkedIn / Twitter). |
| `/api/scheduled` | `GET` | Fetch all queued/scheduled posts. |
| `/api/scheduled/{id}` | `DELETE` | Cancel and delete a queued post. |
| `/publish/{id}` | `POST` | Immediately publish a scheduled post. |
| `/api/published` | `GET` | Retrieve publication history for analytics and logs. |
| `/api/reading-list` | `GET`, `POST` | List or create saved reading list items. |
| `/api/reading-list/{id}`| `PUT`, `DELETE` | Update or delete a reading list item. |
| `/api/auth/status` | `GET` | Check linked social network OAuth status. |
| `/api/auth/init-{provider}` | `GET` | Initiate OAuth flow for Twitter or LinkedIn. |

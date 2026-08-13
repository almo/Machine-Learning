# Networks Module

The `Networks` package contains connector clients responsible for integrating the AI4Media backend with external social media platforms (LinkedIn and Twitter/X).

Each connector encapsulates platform-specific API communication, OAuth token management via `TokenService`, request serialization, media upload workflows, and error handling using Ktor's asynchronous HTTP client.

---

## Files Overview

| File | Purpose | Target APIs |
| :--- | :--- | :--- |
| [`LinkedinConnector.kt`](./LinkedinConnector.kt) | Manages publishing organization posts, sharing posts to user profiles, and uploading image assets. | LinkedIn REST API v2 (`/v2/ugcPosts`, `/v2/assets`, `/v2/socialActions`, `/v2/userinfo`) |
| [`TwitterConnector.kt`](./TwitterConnector.kt) | Manages publishing tweets/posts to user Twitter timelines. | Twitter API v2 (`/2/tweets`) |

---

## Detailed File Descriptions

### 1. [`LinkedinConnector.kt`](./LinkedinConnector.kt)

Singleton object (`LinkedinConnector`) implementing the integration with LinkedIn's v2 REST APIs using Ktor CIO engine and Kotlinx JSON serialization.

#### Key Functions

- **`publishToOrganizationTimeline(userId, textContent, urlContent, tags, imageUrl)`**:
  - Validates and retrieves the user's LinkedIn OAuth token via `TokenService`.
  - Formats content text with optional link notes and hashtags.
  - If `imageUrl` is present, downloads the image binary and initiates an upload sequence via `uploadImage`.
  - Constructs and sends a UGC post request (`POST https://api.linkedin.com/v2/ugcPosts`) targeting the configured organization URN (`urn:li:organization:77043213`).
  - If both an image and a link URL are provided, posts the link as a first comment under the created post (`POST https://api.linkedin.com/v2/socialActions/{postId}/comments`) to maximize engagement without cluttering media display.
  - Returns the published LinkedIn post URN / ID.

- **`shareToUserTimeline(userId, originalPostUrn, comment, tags, authorUrn)`**:
  - Retrieves a valid LinkedIn token for the user.
  - Verifies user identity against the LinkedIn user info endpoint (`GET https://api.linkedin.com/v2/userinfo`).
  - Creates a reshared UGC post referencing the original organization post URL (`https://www.linkedin.com/feed/update/{originalPostUrn}`).
  - Returns the shared post URN / ID.

- **`uploadImage(token, ownerUrn, imageBytes)`**:
  - Step 1: Registers an upload request with LinkedIn Digital Media Asset service (`POST https://api.linkedin.com/v2/assets?action=registerUpload`) using the `urn:li:digitalmediaRecipe:feedshare-image` recipe.
  - Step 2: Extracts the assigned asset URN and the secure upload URL.
  - Step 3: Performs a binary `PUT` request with the image payload.
  - Returns the created asset URN (e.g. `urn:li:digitalmediaAsset:...`) for attachment to UGC posts.

---

### 2. [`TwitterConnector.kt`](./TwitterConnector.kt)

Singleton object (`TwitterConnector`) implementing publishing capabilities for Twitter (X) using the Twitter API v2.

#### Key Functions

- **`publishToTwitterTimeline(userId, textContent, urlContent, tags)`**:
  - Obtains a valid Twitter OAuth token for the user via `TokenService`.
  - Formats the tweet body by combining text, target URL, and hashtags.
  - Submits a `POST` request to Twitter API v2 (`https://api.twitter.com/2/tweets`) with the payload `{ "text": "..." }`.
  - Parses the response and extracts the created tweet ID (`data.id`).
  - Returns the newly created Tweet ID string.

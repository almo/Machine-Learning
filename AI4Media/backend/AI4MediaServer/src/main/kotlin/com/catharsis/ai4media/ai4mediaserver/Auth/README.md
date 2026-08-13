# Auth Module

This directory contains the authentication, authorization, and token management utilities for the **AI4MediaServer** backend. It encapsulates OAuth 2.0 workflows (including PKCE security extensions), token lifecycle management (storage, retrieval, and automated refreshing in Google Cloud Datastore), and Google OpenID Connect (OIDC) token verification for secured inter-service communication.

---

## Files Overview

| File | Type | Description |
| :--- | :--- | :--- |
| [`oAuthPKCE.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/oAuthPKCE.kt) | Kotlin Object (`oAuthPKCE`) | Generates cryptographically secure PKCE (RFC 7636) code verifiers and SHA-256 code challenges for OAuth 2.0 authorization flows. |
| [`TokenService.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenService.kt) | Kotlin Object & Data Models | Handles OAuth 2.0 token persistence, expiration checks, and automatic token refreshes for third-party providers (Twitter/X, LinkedIn) using Google Cloud Datastore and Ktor Client. |
| [`TokenVerifier.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenVerifier.kt) | Kotlin Object (`TokenVerifier`) | Verifies incoming Google OpenID Connect (OIDC) ID tokens (JWTs) to authenticate incoming requests from Google Cloud services (e.g., Google Cloud Tasks). |

---

## Detailed File Descriptions

### 1. [`oAuthPKCE.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/oAuthPKCE.kt)
Implements utility methods for **Proof Key for Code Exchange (PKCE)** based on RFC 7636 to protect OAuth 2.0 public and confidential client authorization flows against authorization code interception attacks.

- **`generateCodeVerifier(): String`**: Uses `java.security.SecureRandom` to generate a 32-byte cryptographically secure random sequence encoded as an unpadded, URL-safe Base64 string.
- **`generateCodeChallenge(verifier: String): String`**: Computes the SHA-256 hash digest of the verifier string and outputs an unpadded, URL-safe Base64 string.

---

### 2. [`TokenService.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenService.kt)
Manages the complete lifecycle and persistence of third-party social provider OAuth 2.0 tokens (Twitter/X and LinkedIn) with Google Cloud Datastore.

- **Data Models**:
  - `OAuthTokenData`: Internal data representation for stored credentials (`accessToken`, `refreshToken`, `provider`, `expiresAt`).
  - `TokenRefreshResponse`: Model mapping JSON response payloads from external token endpoints (`access_token`, `refresh_token`, `expires_in`).
- **Core Capabilities**:
  - **`saveToken(...)`**: Executes a transactional write to Google Cloud Datastore under the `UserTokens` kind, storing or updating tokens keyed by `userId` and provider.
  - **`getValidToken(...)`**: Retrieves the stored access token. It calculates remaining validity against the current timestamp with a 60-second safety margin. If the token has expired or is nearing expiration, it automatically triggers `refreshToken(...)`.
  - **`refreshToken(...)`**: Dispatches a backend-to-backend HTTP POST request using Ktor HTTP Client (CIO engine) with the `refresh_token` grant to the provider's token endpoint (Twitter API v2 or LinkedIn OAuth v2), updates the datastore with the refreshed credentials, and returns the active token.

---

### 3. [`TokenVerifier.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/src/main/kotlin/com/catharsis/ai4media/ai4mediaserver/Auth/TokenVerifier.kt)
Provides validation for Google-issued OpenID Connect (OIDC) tokens.

- **Google ID Token Verification**: Configures a `GoogleIdTokenVerifier` instance against Google's issuer (`https://accounts.google.com`) and matches the target audience (`AppConfig.baseUrl`).
- **`verify(token: String): Boolean`**: Verifies cryptographic signatures, expiration times, issuer, and audience claims of incoming JWT tokens. Used primarily to authenticate and authorize automated triggers and webhook callbacks dispatched from Google Cloud Tasks or other internal GCP services.

# Test Suite: AI4Media Server (`com/example`)

This directory contains automated integration and endpoint tests for the **AI4MediaServer** backend using Ktor's server testing engine (`io.ktor.server.testing`).

---

## Directory Purpose

The primary objective of this directory is to validate HTTP routing, request/response serialization, and application lifecycle behavior for the AI4Media Ktor server backend deployed on Google App Engine. Tests use the embedded `testApplication` runner to spin up the application module without launching an external HTTP server, ensuring fast, isolated, and deterministic test execution.

---

## File Overview

| File | Purpose |
| :--- | :--- |
| [`GoogleAppEngineTest.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt) | Integration test suite verifying HTTP endpoints, routing, and in-memory data store behavior. |

---

## File Details

### [`GoogleAppEngineTest.kt`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt)

Defines the [`GoogleAppEngineTest`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt#L12-L77) class, which executes integration test cases against the Ktor application module (`module()`):

- **[`setup()`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt#L14-L18)** (`@BeforeTest`):
  Clears the global in-memory `socialMediaPosts` collection prior to each test invocation to prevent test state leakage and guarantee isolation.

- **[`testRoot()`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt#L20-L28)** (`@Test`):
  Verifies the root health/welcome endpoint (`GET /`), asserting a status code of `200 OK` and a response body of `"Hello, AI4Media Server!"`.

- **[`testGetSocialMediaPosts_whenEmpty()`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt#L30-L38)** (`@Test`):
  Validates that querying the posts endpoint (`GET /social-media-posts`) when no posts exist returns a `404 Not Found` status with the message `"No social media posts found."`.

- **[`testPostAndGetSocialMediaPosts()`](file:///home/almo/Engineering/Machine-Learning/AI4Media/backend/AI4MediaServer/test/kotlin/com/example/GoogleAppEngineTest.kt#L40-L76)** (`@Test`):
  Tests the complete publish and retrieve lifecycle:
  1. Serializes a `SocialMediaPost` object and issues a `POST /social-media-post` request, verifying a `200 OK` response with `"Post received and stored."`.
  2. Executes a `GET /social-media-posts` request, deserializes the JSON response payload, and validates that the post attributes (text, tag, and default `"pending"` status) match expectations.

---

## Running the Tests

To execute the test suite from the `AI4MediaServer` root directory, run:

```bash
./gradlew test
```

Or target this specific test class:

```bash
./gradlew test --tests "com.catharsis.ai4media.ai4mediaserver.GoogleAppEngineTest"
```

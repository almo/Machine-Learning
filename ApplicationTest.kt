package com.catharsis.ai4media.ai4mediaserver

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ApplicationTest {

    @BeforeTest
    fun setup() {
        // Clear the list before each test to ensure isolation
        socialMediaPosts.clear()
    }

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, AI4Media Server!", response.bodyAsText())
    }

    @Test
    fun testGetSocialMediaPosts_whenEmpty() = testApplication {
        application {
            module()
        }
        val response = client.get("/social-media-posts")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("No social media posts found.", response.bodyAsText())
    }

    @Test
    fun testPostAndGetSocialMediaPosts() = testApplication {
        application {
            module()
        }

        // 1. POST a new social media post
        val newPost = SocialMediaPost(
            text = "This is a test from our unit test!",
            url = "https://example.com/test-post",
            hashtags = listOf("#testing", "#ktor"),
            tag = "UnitTest"
        )

        val postResponse = client.post("/social-media-post") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(newPost))
        }

        assertEquals(HttpStatusCode.OK, postResponse.status)
        assertEquals("Post received and stored.", postResponse.bodyAsText())

        // 2. GET all posts and verify the new post is there
        val getResponse = client.get("/social-media-posts")
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val postsJson = getResponse.bodyAsText()
        val posts = Json.decodeFromString<List<SocialMediaPost>>(postsJson)

        assertEquals(1, posts.size)
        val retrievedPost = posts.first()

        assertEquals("This is a test from our unit test!", retrievedPost.text)
        assertEquals("UnitTest", retrievedPost.tag)
        // The server should set the status to "pending" on new posts
        assertEquals("pending", retrievedPost.status)
    }
}

package com.catharsis.ai4media.ai4mediaserver

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable

@Serializable
data class SocialMediaPost(
    val text: String,
    val url: String?,
    val hashtags: List<String> = emptyList(),
    val tag: String,
    val scheduled: String? = null, // Date and time for posting
    val status: String = "pending", // e.g., "pending", "posted", "failed"
    val socialURL: String? = null // Link to the post online after it's published
)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val socialMediaPosts = mutableListOf<SocialMediaPost>(
    SocialMediaPost(
        text = "This is a mock post for testing purposes.",
        url = "https://example.com/mock-post",
        hashtags = listOf("#mock", "#testing", "#AI4Media"),
        tag = "test",
        status = "posted",
        socialURL = null
    )
)

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Hello, AAI4Media Server!")
        }
        
        get("/social-media-posts") {
            if (socialMediaPosts.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No social media posts found.")
            } else {
                call.respond(socialMediaPosts)
            }
        }

        post("/social-media-post") {
            val post = call.receive<SocialMediaPost>().copy(status = "pending") // Ensure new posts are pending
            socialMediaPosts.add(post)
            call.respond(HttpStatusCode.OK, "Post received and stored.")
        }
    }
}

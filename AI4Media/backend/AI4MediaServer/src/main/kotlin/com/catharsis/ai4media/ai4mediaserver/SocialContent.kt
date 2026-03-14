package com.catharsis.ai4media.ai4mediaserver.content

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Represents a social media post within the AI4Media system.
 *
 * @property id Unique identifier for the social content.
 * @property userId The ID of the user who owns this content.
 * @property textContent The main text body of the post.
 * @property targetUrn The Uniform Resource Name (URN) identifying the post target or location.
 * @property scheduledTime The time when the post is scheduled to be published.
 * @property createdTime The timestamp when this record was created.
 * @property media Optional media content (Image, Video, or Document) attached to the post.
 * @property firstComment An optional comment to be posted immediately after the main post.
 * @property tags A list of tags or hashtags associated with the post.
 * @property status The current lifecycle status of the post (e.g., DRAFT, SCHEDULED).
 */
@Serializable
data class SocialContentRequest(
    val userId: String,
    val textContent: String,
    val urlContent: String,
    val scheduledTime: String,
    val tags: List<String> = emptyList()
)

data class SocialContent(
        val id: UUID = UUID.randomUUID(),
        val userId: String,
        val textContent: String,
        val targetUrn: String,
        val scheduledTime: LocalDateTime,
        val createdTime: LocalDateTime,
        val media: MediaContent? = null,
        val firstComment: String? = null,
        val tags: List<String> = emptyList(),
        val status: PostStatus = PostStatus.DRAFT
)

/**
 * Represents the media content associated with a social post. Supported types are [Image], [Video],
 * and [Document].
 */
sealed class MediaContent {
    /**
     * Represents an image media item.
     *
     * @property url The publicly accessible URL of the image.
     * @property altText Alternative text for accessibility and SEO.
     */
    data class Image(val url: String, val altText: String?) : MediaContent()

    /**
     * Represents a video media item.
     *
     * @property url The publicly accessible URL of the video.
     * @property title The title or caption of the video.
     */
    data class Video(val url: String, val title: String?) : MediaContent()

    /**
     * Represents a generic document (e.g., PDF).
     *
     * @property url The publicly accessible URL of the document.
     * @property title The title or name of the document.
     */
    data class Document(val url: String, val title: String?) : MediaContent()
}

/** Enumerates the possible states of a [SocialContent] post. */
enum class PostStatus {
    /** The post is being drafted and is not yet ready for scheduling. */
    DRAFT,
    /** The post is scheduled for publication at a specific time. */
    SCHEDULED,
    /** The post has been successfully published to the target platform. */
    PUBLISHED,
    /** The post failed to publish. */
    FAILED
}

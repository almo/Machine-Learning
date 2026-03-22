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
 * @property urlContent The url to the context of the post.
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
    val tags: String="",
    val networks: String=""
)

data class SocialContent(
        val id: UUID = UUID.randomUUID(),
        val userId: String,
        val textContent: String,
        val urlContent: String,
        val targetUrn: String? = null,
        val scheduledTime: LocalDateTime,
        val createdTime: LocalDateTime,        
        val firstComment: String? = null,
        val tags: List<String> = emptyList(),
        val status: PostStatus = PostStatus.DRAFT,        
        val network: SocialNetwork = SocialNetwork.LINKEDIN
)

/** Enumerates the possible states of a [SocialContent] post. */
enum class PostStatus {
    /** The post is being drafted and is not yet ready for scheduling. */
    DRAFT,
    /** The post is scheduled for publication at a specific time. */
    SCHEDULED,
    /** The post has been successfully published to the target platform. */
    PUBLISHED,
    /** The post failed to publish. */
    FAILED,
    /** The post is currently being published. */
    PUBLISHING
}

/** Enumerates the supported social networks for publishing. */
enum class SocialNetwork {
    LINKEDIN,
    TWITTER
}

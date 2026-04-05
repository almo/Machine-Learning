package com.catharsis.ai4media.ai4mediaserver.content

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}

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

@Serializable
data class SocialContent(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val textContent: String,
        val urlContent: String,
        val targetUrn: String? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val scheduledTime: LocalDateTime,
        @Serializable(with = LocalDateTimeSerializer::class) val createdTime: LocalDateTime,        
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
    /** The post is being automatically scheduled for publication. */
    AUTOSCHEDULED,
    /** The post has been successfully published to the target platform. */
    PUBLISHED,
    /** The post failed to publish. */
    FAILED,
    /** The post is currently being published. */
    PUBLISHING,
    /** The post has been deleted. */
    DELETED
}

/** Enumerates the supported social networks for publishing. */
@Serializable
enum class SocialNetwork {
    LINKEDIN,
    TWITTER
}

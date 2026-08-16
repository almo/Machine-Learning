package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.content.*
import com.google.cloud.Timestamp
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StructuredQuery
import com.google.cloud.datastore.StringValue
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date

sealed class ClaimPublishResult {
    data class Claimed(val entity: Entity, val lockTimestamp: Timestamp) : ClaimPublishResult()
    object AlreadyPublished : ClaimPublishResult()
    object Cancelled : ClaimPublishResult()
    data class CurrentlyInFlight(val lockedAtSeconds: Long) : ClaimPublishResult()
    object NotFound : ClaimPublishResult()
}

object DataStoreWrapper {
    val datastore = DatastoreOptions.getDefaultInstance().service

    /**
     * Atomically claims a SocialContent post for publishing within a Datastore ACID transaction.
     * Transitions status to PUBLISHING and sets a 90-second lease timeout, preventing race conditions
     * and duplicate social publications from concurrent Cloud Tasks deliveries.
     *
     * @param postId The ID of the post to claim.
     * @param leaseTimeoutSeconds Timeout after which a crashed PUBLISHING lock is considered stale and reclaimable (default 90s).
     */
    fun claimPostForPublishing(postId: String, leaseTimeoutSeconds: Long = 90): ClaimPublishResult {
        val keyFactory = datastore.newKeyFactory().setKind("SocialContent")
        val key = postId.toLongOrNull()?.let { keyFactory.newKey(it) } ?: keyFactory.newKey(postId)

        val transaction = datastore.newTransaction()
        try {
            val entity = transaction.get(key) ?: run {
                if (transaction.isActive) transaction.rollback()
                return ClaimPublishResult.NotFound
            }

            val currentStatus = if (entity.contains("status")) entity.getString("status") else ""

            if (currentStatus == PostStatus.PUBLISHED.name) {
                if (transaction.isActive) transaction.rollback()
                return ClaimPublishResult.AlreadyPublished
            }

            if (currentStatus == PostStatus.DELETED.name) {
                if (transaction.isActive) transaction.rollback()
                return ClaimPublishResult.Cancelled
            }

            // Check if there is an active (fresh) in-flight lock
            val isStaleLock = if (currentStatus == PostStatus.PUBLISHING.name && entity.contains("lockedAt")) {
                val lockedAtSeconds = entity.getTimestamp("lockedAt").seconds
                (Instant.now().epochSecond - lockedAtSeconds) > leaseTimeoutSeconds
            } else false

            if (currentStatus == PostStatus.PUBLISHING.name && !isStaleLock) {
                val lockedAtSeconds = if (entity.contains("lockedAt")) entity.getTimestamp("lockedAt").seconds else 0L
                if (transaction.isActive) transaction.rollback()
                return ClaimPublishResult.CurrentlyInFlight(lockedAtSeconds)
            }

            // Allow claiming if SCHEDULED, AUTOSCHEDULED, FAILED (retry), or Stale Lock (> 90s)
            val lockTimestamp = Timestamp.now()
            val updatedEntity = Entity.newBuilder(entity)
                .set("status", PostStatus.PUBLISHING.name)
                .set("lockedAt", lockTimestamp)
                .build()

            transaction.put(updatedEntity)
            transaction.commit()
            return ClaimPublishResult.Claimed(updatedEntity, lockTimestamp)
        } finally {
            if (transaction.isActive) {
                transaction.rollback()
            }
        }
    }

    /**
     * Atomically marks a post as PUBLISHED with optimistic fencing check on the lockTimestamp.
     */
    fun completePublishing(
        postId: String,
        lockTimestamp: Timestamp,
        targetUrn: String?,
        profile: SocialProfile? = null
    ): Boolean {
        val keyFactory = datastore.newKeyFactory().setKind("SocialContent")
        val key = postId.toLongOrNull()?.let { keyFactory.newKey(it) } ?: keyFactory.newKey(postId)
        val transaction = datastore.newTransaction()

        try {
            val entity = transaction.get(key) ?: return false

            // Fencing check: Ensure the lock was not stolen by another worker
            if (entity.contains("lockedAt") && entity.getTimestamp("lockedAt") != lockTimestamp) {
                return false
            }

            val updatedEntityBuilder = Entity.newBuilder(entity)
                .set("status", PostStatus.PUBLISHED.name)

            if (targetUrn != null) {
                updatedEntityBuilder.set("targetUrn", targetUrn)
            }
            if (profile != null) {
                updatedEntityBuilder.set("profile", profile.name)
            }

            transaction.put(updatedEntityBuilder.build())
            transaction.commit()
            return true
        } finally {
            if (transaction.isActive) {
                transaction.rollback()
            }
        }
    }

    fun saveSocialContent(
            userId: String,
            textContent: String,
            urlContent: String?,
            scheduledTime: LocalDateTime,
            network: SocialNetwork,
            profile: SocialProfile,
            tags: List<String>
    ): String {

         var content: SocialContent? = null

        try {
            content =
                SocialContent(
                        userId = userId,
                        textContent = textContent,
                        urlContent = urlContent?: "",
                        targetUrn = null,
                        scheduledTime = scheduledTime,
                        createdTime = LocalDateTime.now(AppConfig.timeZone), 
                        tags = tags,
                        network = network, 
                        profile = profile
                )

            val key = datastore.newKeyFactory().setKind("SocialContent").newKey(content.id.toString())

            val entity = Entity.newBuilder(key)
                .set("userId", content.userId)
                .set("textContent",content.textContent)
                .set("urlContent", content.urlContent)
                .set("scheduledTime", Timestamp.of(Date.from(content.scheduledTime.atZone(AppConfig.timeZone).toInstant())))
                .set("createdTime", Timestamp.of(Date.from(content.createdTime.atZone(AppConfig.timeZone).toInstant())))
                .set("status", content.status.name)
                .set("network",content.network.name)
                .set("profile",content.profile.name)
                .set("tags",content.tags.map {StringValue.of(it)})
                .build()

            datastore.put(entity)
        } catch (e: Exception) {
            throw e
        }

        return content?.id.toString()
    }

    fun updateStatus(id: String, status: PostStatus, targetUrn: String? = null, cloudTaskName: String? = null) {
        val key = datastore.newKeyFactory().setKind("SocialContent").newKey(id)
        val transaction = datastore.newTransaction()

        try {
            val entity = transaction.get(key)

            if (entity != null) {
                val updatedEntityBuilder = Entity.newBuilder(entity)
                    .set("status", status.name)

                if (targetUrn != null) {
                    updatedEntityBuilder.set("targetUrn", targetUrn)
                }
                if (cloudTaskName != null) {
                    updatedEntityBuilder.set("cloudTaskName", cloudTaskName)
                }
                val updatedEntity = updatedEntityBuilder.build()
                transaction.put(updatedEntity)
                transaction.commit()
            } else {
                throw IllegalArgumentException("SocialContent with ID $id not found.")
            }
        } finally {
            if (transaction.isActive) {
                transaction.rollback()
            }
        }
    }

    fun getPostsForScheduling(userId: String, network: SocialNetwork): List<LocalDateTime> {
        val startOfDay = LocalDateTime.now(AppConfig.timeZone).toLocalDate().atStartOfDay()
        val timestampLimit = Timestamp.of(Date.from(startOfDay.atZone(AppConfig.timeZone).toInstant()))

        val query = Query.newEntityQueryBuilder()
            .setKind("SocialContent")
            .setFilter(
                StructuredQuery.CompositeFilter.and(
                    StructuredQuery.PropertyFilter.eq("userId", userId),
                    StructuredQuery.PropertyFilter.eq("network", network.name),
                    StructuredQuery.PropertyFilter.ge("scheduledTime", timestampLimit)
                )
            )
            .setOrderBy(StructuredQuery.OrderBy.asc("scheduledTime"))
            .build()

        val results = datastore.run(query)
        val posts = mutableListOf<LocalDateTime>()

        while (results.hasNext()) {
            val entity = results.next()
            val status = if (entity.contains("status")) entity.getString("status") else ""
            if (status in listOf(PostStatus.PUBLISHED.name, PostStatus.PUBLISHING.name, PostStatus.SCHEDULED.name, PostStatus.AUTOSCHEDULED.name)) {
                if (entity.contains("scheduledTime")) {
                    val googleTimestamp = entity.getTimestamp("scheduledTime")
                    val instant = Instant.ofEpochSecond(googleTimestamp.seconds, googleTimestamp.nanos.toLong())
                    val scheduledTime = LocalDateTime.ofInstant(instant, AppConfig.timeZone)
                    posts.add(scheduledTime)
                }
            }
        }
        return posts
    }
}

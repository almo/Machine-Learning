package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.content.*
import com.google.cloud.Timestamp
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StringValue
import java.util.Date
import java.time.LocalDateTime

object DataStoreWrapper {
    val datastore = DatastoreOptions.getDefaultInstance().service

    fun saveSocialContent(
            userId: String,
            textContent: String,
            urlContent: String?,
            scheduledTime: LocalDateTime,
            network: SocialNetwork,
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
                        network = network 
                )

            val key = datastore.newKeyFactory().setKind("SocialContent").newKey(content.id.toString())

            val entity = Entity.newBuilder(key)
                .set("userId", content.userId)
                .set("textContent",content.textContent)
                .set("urlContent", content.urlContent)
                .set("scheduledTime", Timestamp.of(Date.from(content.scheduledTime.atZone(AppConfig.timeZone).toInstant())))
                .set("createdTime", Timestamp.of(Date.from(content.createdTime.atZone(AppConfig.timeZone).toInstant())))
                .set("status", content.status.name)
                .set("network",content.network.name).set("tags",content.tags.map {StringValue.of(it)})
                .build()

            datastore.put(entity)
        } catch (e: Exception) {
            throw e
        }

        return content?.id.toString()
    }

    fun updateStatus(id: String, status: PostStatus, targetUrn: String? = null) {
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
}

package com.catharsis.ai4media.ai4mediaserver.persistence

import com.catharsis.ai4media.ai4mediaserver.model.Source
import com.catharsis.ai4media.ai4mediaserver.model.SourceRequest
import com.google.cloud.datastore.*
import com.google.cloud.datastore.Query
import java.util.UUID

/**
 * A repository for managing Source entities in Google Cloud Datastore.
 */
object DatastoreSourceRepository {

    private val datastore = DatastoreOptions.getDefaultInstance().service
    
    private const val KIND = "RSSFeedSource"
    
    /**
     * Creates a new Source entity in Datastore.
     */
    fun create(sourceRequest: SourceRequest, userId: String): Source {
        val id = UUID.randomUUID().toString()
        val key = datastore.newKeyFactory().setKind(KIND).newKey(id)

        val entity = Entity.newBuilder(key)
            .set("userId", userId)
            .set("name", sourceRequest.name)
            .set("url", sourceRequest.url)
            .set("category", sourceRequest.category)
            .set("tags", sourceRequest.tags)
            .build()
        
            datastore.put(entity)
        
        return entityToSource(entity)
    }

    /**
     * Finds all sources belonging to a specific user.
     */
    fun findByUserId(userId: String): List<Source> {
        val query = Query.newEntityQueryBuilder()
            .setKind(KIND)
            .setFilter(StructuredQuery.PropertyFilter.eq("userId", userId))
            .build()
        val results = datastore.run(query)
        
        return results.asSequence().map { entityToSource(it) }.toList()
    }

    /**
     * Finds a single source by its unique ID.
     */
    fun findById(id: String): Source? {
        val keyFactory = datastore.newKeyFactory().setKind(KIND)
        val key = keyFactory.newKey(id) 
        val entity = datastore.get(key)
        return entity?.let { entityToSource(it) }
    }

    /**
     * Updates an existing source. Note: Ownership should be verified before calling this method.
     */
    fun update(id: String, sourceRequest: SourceRequest): Source {
        val keyFactory = datastore.newKeyFactory().setKind(KIND)
        
        val key = keyFactory.newKey(id)        
        val existingSource = findById(id) ?: throw IllegalArgumentException("Source not found")
        
        val entity = Entity.newBuilder(key)
            // We must preserve the original userId
            .set("userId", existingSource.userId)
            .set("name", sourceRequest.name)
            .set("url", sourceRequest.url)
            .set("category", sourceRequest.category)
            .set("tags", sourceRequest.tags)
            .build()

        datastore.update(entity)
        return entityToSource(entity)
    }

    /**
     * Deletes a source by its ID. Note: Ownership should be verified before calling this method.
     */
    fun delete(id: String) {
        val keyFactory = datastore.newKeyFactory().setKind(KIND)        
        val key = keyFactory.newKey(id)        
        datastore.delete(key)
    }

    private fun entityToSource(entity: Entity): Source {
        return Source(
            id = entity.key.name ?: entity.key.id.toString(),
            userId = if (entity.contains("userId") && !entity.isNull("userId")) entity.getString("userId") else "",
            name = if (entity.contains("name") && !entity.isNull("name")) entity.getString("name") else "",
            url = if (entity.contains("url") && !entity.isNull("url")) entity.getString("url") else "",
            category = if (entity.contains("category") && !entity.isNull("category")) entity.getString("category") else "",
            tags = if (entity.contains("tags") && !entity.isNull("tags")) {
                try {
                    entity.getString("tags")
                } catch (e: Exception) { "" } // Safely handle if 'tags' is stored incorrectly as a List/Array by legacy data
            } else ""
        )
    }
}
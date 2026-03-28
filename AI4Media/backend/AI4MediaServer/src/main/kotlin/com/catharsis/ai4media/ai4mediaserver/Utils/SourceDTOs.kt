package com.catharsis.ai4media.ai4mediaserver.model

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val id: String,
    val userId: String,
    val name: String,
    val url: String,
    val category: String,
    val tags: String
)

@Serializable
data class SourceRequest(
    val name: String,
    val url: String,
    val category: String,
    val tags: String
)
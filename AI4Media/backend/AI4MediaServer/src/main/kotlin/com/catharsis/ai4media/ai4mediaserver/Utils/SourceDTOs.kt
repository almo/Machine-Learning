package com.catharsis.ai4media.ai4mediaserver.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString())
    }
}

@Serializable
data class Source(
    val id: String,
    val userId: String,
    val name: String,
    val url: String,
    val category: String,
    val tags: String,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val lastSyncTime: ZonedDateTime? = null,
    val syncStatus: String? = null
)

@Serializable
data class SourceRequest(
    val name: String,
    val url: String,
    val category: String,
    val tags: String
)
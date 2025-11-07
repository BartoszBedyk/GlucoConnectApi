package model

import InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ActivityEntity(
    val id: Int,
    val value: String,
    val userId: Int,
    @Serializable(InstantSerializer::class)
    val createdAt: Instant
)

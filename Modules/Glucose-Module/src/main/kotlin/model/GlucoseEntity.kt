package model

import InstantSerializer
import UUIDSerializer
import data.GlucoseUnit
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class GlucoseEntity(
    @Serializable(with = UUIDSerializer::class) val id: UUID?,
    val concentration: Double,
    val unit: GlucoseUnit,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val afterMedication: Boolean,
    val afterMeal: Boolean,
    val note: String?,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant?,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant?
)

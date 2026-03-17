package model

import data.GlucoseUnit
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class GlucoseEntity(
    @Contextual val id: UUID?,
    val concentration: Double,
    val unit: GlucoseUnit,
    @Contextual
    val timestamp: Instant,
    val afterMedication: Boolean,
    val afterMeal: Boolean,
    val note: String?,
    @Contextual
    val createdAt: Instant?,
    @Contextual
    val updatedAt: Instant?
)

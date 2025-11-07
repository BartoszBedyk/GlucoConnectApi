package model

import data.GlucoseUnit
import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CreateGlucoseRequest(
    val concentration: Double,
    val unit: GlucoseUnit,
    @Contextual
    val timestamp: Instant,
    val afterMedication: Boolean,
    val afterMeal: Boolean,
    val note: String?,
    @Contextual
    val createdAt: Instant? = null,
    @Contextual
    val updatedAt: Instant? = null
)

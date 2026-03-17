package model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class HeartbeatEntity(
    @Contextual val id: UUID?,
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    @Contextual val timestamp: Instant,
    val note: String?,
    @Contextual
    val createdAt: Instant?,
    @Contextual
    val updatedAt: Instant?
)

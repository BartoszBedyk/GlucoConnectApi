package model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class CreateHeartbeatRequest(
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    @Contextual
    val timestamp: Instant,
    val note: String?,
    @Contextual
    val createdAt: Instant? = null,
    @Contextual
    val updatedAt: Instant? = null
)

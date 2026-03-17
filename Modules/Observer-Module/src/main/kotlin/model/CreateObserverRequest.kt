package model

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CreateObserverRequest(
    @Contextual val observedId: UUID,
    @Contextual val createdAt: Instant?,
    @Contextual val updatedAt: Instant?
)

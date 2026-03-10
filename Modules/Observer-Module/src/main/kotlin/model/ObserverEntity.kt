package model

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlinx.serialization.Contextual

@Serializable
data class ObserverEntity(
    @Contextual
    private val id: UUID,
    @Contextual
    private val observer: UUID,
    @Contextual
    private val observed: UUID,
    private val isAccepted: Boolean,
)

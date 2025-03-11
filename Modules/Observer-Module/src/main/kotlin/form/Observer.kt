package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Observer(
    @Serializable(with = UUIDSerializer::class)
    private val id : UUID,
    @Serializable(with = UUIDSerializer::class)
    private val observerId: UUID,
    @Serializable(with = UUIDSerializer::class)
    private val observedId: UUID,
    private val isAccepted: Boolean,
    )

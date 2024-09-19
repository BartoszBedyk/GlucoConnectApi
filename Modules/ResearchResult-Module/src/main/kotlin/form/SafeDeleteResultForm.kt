package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SafeDeleteResultForm(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)








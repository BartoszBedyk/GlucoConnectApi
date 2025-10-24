package form

import UUIDSerializer
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class SafeDeleteResultForm(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)








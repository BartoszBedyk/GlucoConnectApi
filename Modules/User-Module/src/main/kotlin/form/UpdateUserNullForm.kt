package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UpdateUserNullForm(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val firstName: String,
    val lastName: String,
    val prefUint: String,
    val diabetes: String,
)

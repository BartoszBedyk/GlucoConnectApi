package form

import UUIDSerializer
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class UpdateUserNullForm(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val firstName: String,
    val lastName: String,
    val prefUnit: String,
    val diabetes: String,
)

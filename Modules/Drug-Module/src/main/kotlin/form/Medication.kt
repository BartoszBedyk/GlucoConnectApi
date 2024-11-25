package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Medication(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val description: String?,
    val manufacturer: String?,
    val form: String?,
    val strength: String?
)

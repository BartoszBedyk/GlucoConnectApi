package form

import UUIDSerializer
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class CreateUserStepTwoForm(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val firstName: String,
    val lastName: String,
    val prefUnit: String,
    val diabetes: String,
    val userType: String
)

package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

data class User(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val password: String,
    val type: UserType?,
    val isBlocked: Boolean
)

enum class UserType{
    ADMIN, PATIENT, DOCTOR, OBSERVER
}
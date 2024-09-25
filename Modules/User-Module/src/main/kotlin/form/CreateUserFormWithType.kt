package form

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserFormWithType(
    val email: String,
    val password: String,
    val userType: UserType
)

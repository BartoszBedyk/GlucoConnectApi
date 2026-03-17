package model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: UserType,
    val prefUnit: GlucoseUnit
)

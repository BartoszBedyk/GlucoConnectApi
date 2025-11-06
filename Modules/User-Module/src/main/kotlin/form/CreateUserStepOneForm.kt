package form

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserStepOneForm(val email: String, val password: String)

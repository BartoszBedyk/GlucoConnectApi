package model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationCredentials(val username: String, val password: String)

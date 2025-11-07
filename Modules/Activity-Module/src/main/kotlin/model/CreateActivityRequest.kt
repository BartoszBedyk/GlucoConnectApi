package model

import kotlinx.serialization.Serializable

@Serializable
data class CreateActivityRequest(val value: String, val userId: Int)

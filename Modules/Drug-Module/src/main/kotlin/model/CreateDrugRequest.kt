package model

import kotlinx.serialization.Serializable

@Serializable
data class CreateDrugRequest(
    val name : String,
    val description : String?,
    val manufacturer: String,
    val form: String,
    val strength: String,
)

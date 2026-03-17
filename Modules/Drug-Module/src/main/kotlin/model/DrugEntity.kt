package model

import kotlinx.serialization.Serializable

@Serializable
data class DrugEntity(
    val id: Long,
    val name : String,
    val description : String?,
    val manufacturer: String,
    val form: String,
    val strength: String,
)

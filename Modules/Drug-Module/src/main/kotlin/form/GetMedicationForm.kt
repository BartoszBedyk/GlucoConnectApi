package form

import UUIDSerializer
import kotlinx.serialization.Serializable

@Serializable
data class GetMedicationForm(
    @Serializable(with = UUIDSerializer::class)
    val userId: String,
    @Serializable(with = UUIDSerializer::class)
    val medicationId: String,
)

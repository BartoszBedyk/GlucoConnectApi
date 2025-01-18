package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GetMedicationForm(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val medicationId: UUID,
)

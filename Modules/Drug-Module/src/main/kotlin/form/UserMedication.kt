package form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class UserMedication(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,

    @Serializable(with = UUIDSerializer::class)
    val medicationId: UUID,

    val dosage: String,
    val frequency: String,

    @Serializable(with = DateSerializer::class)
    val startDate: Date,

    @Serializable(with = DateSerializer::class)
    val endDate: Date?,

    val notes: String?,

    val medicationName: String,

    val description: String?,

    val manufacturer: String?,

    val form: String?,

    val strength: String?
)


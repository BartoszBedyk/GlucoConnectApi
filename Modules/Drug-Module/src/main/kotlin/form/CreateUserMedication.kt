package form

import DateSerializer
import UUIDSerializer
import java.util.Date
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class CreateUserMedication(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val medicationId: UUID,
    val dosage: String,
    val frequency: String,
    @Serializable(with = DateSerializer::class)
    val startDate: Date?,
    @Serializable(with = DateSerializer::class)
    val endDate: Date?,
    val notes: String?
)

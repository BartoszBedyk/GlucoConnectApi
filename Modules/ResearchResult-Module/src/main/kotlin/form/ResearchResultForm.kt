package form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class ResearchResultForm(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class) val timestamp: Date,
    val afterMedication: Boolean,
    val emptyStomach: Boolean,
    val notes: String?
)

package form

import DateSerializer
import UUIDSerializer
import java.util.Date
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class UpdateResearchResultForm(
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class)
    val timestamp: Date,
    val afterMedication: Boolean,
    val emptyStomach: Boolean,
    val notes: String?,
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
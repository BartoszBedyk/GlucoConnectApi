package form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UpdateResearchResultForm(

    val sequenceNumber: Int,
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class)
    val timestamp: Date,
    @Serializable(with = UUIDSerializer::class)
    val Id: UUID,
)

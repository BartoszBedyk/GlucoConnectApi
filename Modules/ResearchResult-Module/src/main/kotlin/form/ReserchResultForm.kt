package form

import DateSerializer
import java.util.*
import kotlinx.serialization.Serializable


@Serializable
data class ResearchResultForm(
    val sequenceNumber: Int,
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class) val timestamp: Date
)

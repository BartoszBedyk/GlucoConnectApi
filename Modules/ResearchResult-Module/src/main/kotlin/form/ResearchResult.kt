package form

import DateSerializer
import UUIDSerializer
import java.util.*
import kotlinx.serialization.Serializable


@Serializable
data class ResearchResult(
    @Serializable(with = UUIDSerializer::class)
    val Id : UUID,
    val SequenceNumber: Int,
    val GlucoseConcentration : Double,
    val Unit : String,
    @Serializable(with = DateSerializer::class) val timestamp: Date
)

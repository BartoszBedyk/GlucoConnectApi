package form

import DateSerializer
import UUIDSerializer
import java.util.*
import kotlinx.serialization.Serializable


@Serializable
data class ResearchResult(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val sequenceNumber: Int,
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class) val timestamp: Date,
    @Serializable(with = UUIDSerializer::class)val userId: UUID?,
    @Serializable(with = DateSerializer::class) val deletedOn: Date?,
    @Serializable(with = DateSerializer::class) val lastUpdatedOn: Date?
)


enum class PrefUnitType{
    MG_PER_DL, MMOL_PER_L
}




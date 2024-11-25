package form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class HeartbeatForm(
    @Serializable(with = UUIDSerializer::class) val userId: UUID,
    @Serializable(with = DateSerializer::class) val timestamp: Date,
    val systolicPressure : Int,
    val diastolicPressure : Int,
    val pulse : Int,
    val note: String
)
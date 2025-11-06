package form

import DateSerializer
import UUIDSerializer
import java.util.Date
import java.util.UUID
import kotlinx.serialization.Serializable


@Serializable
data class HeartbeatReturn(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val userId: UUID,
    @Serializable(with = DateSerializer::class) val timestamp: Date,
    val systolicPressure : Int,
    val diastolicPressure : Int,
    val pulse : Int,
    val note: String
)

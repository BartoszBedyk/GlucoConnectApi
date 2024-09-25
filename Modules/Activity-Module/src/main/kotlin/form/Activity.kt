package form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Activity(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val type: ActivityType?,
    @Serializable(with = DateSerializer::class) val creationDate: Date,
    @Serializable(with = UUIDSerializer::class) val createdById: UUID
)

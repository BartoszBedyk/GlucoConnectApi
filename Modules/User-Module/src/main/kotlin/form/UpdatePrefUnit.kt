package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UpdatePrefUnit(@Serializable(with = UUIDSerializer::class) val id: UUID, val newUnit: PrefUnitType)

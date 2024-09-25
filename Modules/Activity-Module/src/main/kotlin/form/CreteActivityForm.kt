package form

import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreteActivityForm(
    val type: ActivityType,
)

package form


import kotlinx.serialization.Serializable


@Serializable
data class CreteActivityForm(
    val type: ActivityType,
)

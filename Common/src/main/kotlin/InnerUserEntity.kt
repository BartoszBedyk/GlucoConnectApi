import kotlinx.serialization.Serializable

@Serializable
data class InnerUserEntity(val id: String, val email: String, val type: String)

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String, val details: String? = null)

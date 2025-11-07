
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable


suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    message: String
) {
    respond(status, ApiError(status.value, message))
}

suspend fun ApplicationCall.respondValidationError(message: String) {
    respondError(HttpStatusCode.BadRequest, message)
}

suspend fun ApplicationCall.respondBadRequest(message: String) =
    respond(HttpStatusCode.BadRequest, mapOf("error" to message))

suspend fun ApplicationCall.respondNotFound(message: String) =
    respond(HttpStatusCode.NotFound, mapOf("error" to message))


@Serializable
data class ApiError(
    val status: Int,
    val message: String
)

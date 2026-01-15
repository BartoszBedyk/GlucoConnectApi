import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import java.util.UUID

fun ApplicationCall.extractUserId(jwtHelper: JwtHelper): UUID? {
    val token = request.headers[HttpHeaders.Authorization]
        ?.removePrefix("Bearer ")
        ?.trim()
        ?: return null

    val user = jwtHelper.getUserFromToken(token)
    return UUID.fromString(user.id)
}


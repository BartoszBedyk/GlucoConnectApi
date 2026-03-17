package presentation

import UserPrincipal
import domain.HeartbeatService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateHeartbeatRequest
import pageable.pageRequest
import respondError
import respondValidationError
import java.util.UUID

fun Route.heartbeatController(heartbeatService: HeartbeatService) {
    route("/heartbeats") {
        post {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateHeartbeatRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = heartbeatService.createHeartbeat(request, principal.id)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            heartbeatService.getHeartbeatById(id, principal.id)
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respondError(HttpStatusCode.NotFound, "Heartbeat record not found")
        }

        get("/user") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val pageRequest = call.pageRequest()
            heartbeatService.getHeartbeatsByUserId(pageRequest, principal.id)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

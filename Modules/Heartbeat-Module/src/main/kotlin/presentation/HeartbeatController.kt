package presentation

import domain.HeartbeatService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
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
            val request = runCatching { call.receive<CreateHeartbeatRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = heartbeatService.createHeartbeat(request)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            heartbeatService.getHeartbeatById(id)
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respondError(HttpStatusCode.NotFound, "Heartbeat record not found")
        }

        get("/user/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            val pageRequest = call.pageRequest()
            heartbeatService.getHeartbeatsByUserId(pageRequest, id)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

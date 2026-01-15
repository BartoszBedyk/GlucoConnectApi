package presentation

import domain.HeartbeatService
import JwtHelper
import extractUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import model.CreateHeartbeatRequest
import pageable.pageRequest
import respondError
import respondValidationError

fun Route.heartbeatController(heartbeatService: HeartbeatService, jwtHelper: JwtHelper) {
    route("/heartbeats") {
        post {

            val userId = call.extractUserId(jwtHelper)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateHeartbeatRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = heartbeatService.createHeartbeat(request, userId)
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

        get("/user") {
            val userId = call.extractUserId(jwtHelper)
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val pageRequest = call.pageRequest()
            heartbeatService.getHeartbeatsByUserId(pageRequest, userId)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

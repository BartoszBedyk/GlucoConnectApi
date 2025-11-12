package presentation

import domain.GlucoseService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateGlucoseRequest
import respondError
import respondValidationError
import java.util.UUID

fun Route.glucoseController(glucoseService: GlucoseService) {
    route("/glucoses") {
        post {
            val request = runCatching { call.receive<CreateGlucoseRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = glucoseService.createGlucose(request)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            glucoseService.getGlucoseById(id)
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respondError(HttpStatusCode.NotFound, "Glucose record not found")
        }
    }
}

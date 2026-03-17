package presentation

import UserPrincipal
import domain.GlucoseService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateGlucoseRequest
import pageable.pageRequest
import respondError
import respondValidationError
import java.util.UUID

fun Route.glucoseController(glucoseService: GlucoseService) {
    route("/glucoses") {
        post {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateGlucoseRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = glucoseService.createGlucose(request, principal.id)
            call.respond(HttpStatusCode.Created, created)
        }

        get {
            val pageRequest = call.pageRequest()
            val response = glucoseService.getAllGlucoses(pageRequest)
            call.respond(HttpStatusCode.OK, response)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            } ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            glucoseService.getGlucoseById(id, principal.id)
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respondError(HttpStatusCode.NotFound, "Glucose record not found")
        }

        get("/user") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val pageRequest = call.pageRequest()
            glucoseService.getGlucosesByUserId(pageRequest, principal.id)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

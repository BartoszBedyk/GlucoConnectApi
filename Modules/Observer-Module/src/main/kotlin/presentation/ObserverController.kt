package presentation

import UserPrincipal
import domain.ObserverService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import model.CreateObserverRequest
import respondValidationError

fun Route.observerController(observerService: ObserverService) {
    route("/observer") {
        post {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateObserverRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val created = observerService.createObserver(request, principal.id)
            call.respond(HttpStatusCode.Created, created)
        }

        get{
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val accepted = call.request.queryParameters["accepted"]?.toBooleanStrictOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid or missing 'accepted' parameter")

            val requests = observerService.findObservationRequest(principal.id, accepted)
            call.respond(HttpStatusCode.OK, requests)
        }


        patch("/accept/{id}") {
            val principal = call.principal<UserPrincipal>()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")

            observerService.acceptObservation(UUID.fromString(id), principal.id)
            call.respond(HttpStatusCode.Accepted)
        }

        patch("/unaccept/{id}") {
            val principal = call.principal<UserPrincipal>()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing id")

            observerService.unacceptObservation(UUID.fromString(id), principal.id)
            call.respond(HttpStatusCode.Accepted)
        }


    }
}

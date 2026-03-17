package presentation

import UserPrincipal
import domain.DrugService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateDrugRequest
import model.UserType
import respondError
import respondValidationError

fun Route.drugController(drugService: DrugService) {
    route("/drugs") {
        post {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateDrugRequest>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            if (principal.userType != UserType.ADMIN.toString()) {
                println(principal.userType)
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid user type")
            }
            val created = drugService.createDrug(request)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respondValidationError("Invalid or missing 'id' parameter")

            drugService.getDrugById(id)
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respondError(HttpStatusCode.NotFound, "Drug record not found")

        }
    }
}


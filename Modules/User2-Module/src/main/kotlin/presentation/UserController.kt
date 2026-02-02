package presentation

import UserPrincipal
import domain.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateUserRequest
import respondBadRequest
import respondNotFound
import java.util.UUID

fun Route.userController(userService: UserService) {
    route("/users") {
        post {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val request = runCatching { call.receive<CreateUserRequest>() }
                .getOrElse {
                    return@post call.respondBadRequest("Invalid request body")
                }

            val created = userService.createUser(request, principal.id)
            call.respond(HttpStatusCode.Created, created)
        }

        get {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")

            val user = userService.getUserById(principal.id)
                ?: return@get call.respondNotFound("Bro you doesn't exist anymore...")

            call.respond(HttpStatusCode.OK, user)
        }

        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respondBadRequest("Invalid or missing 'id' parameter")

            val user = userService.getUserById(UUID.fromString(id))
                ?: return@get call.respondNotFound("User not found")

            call.respond(HttpStatusCode.OK, user)
        }
    }
}

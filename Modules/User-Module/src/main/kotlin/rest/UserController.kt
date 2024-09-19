package rest

import form.CreateUserForm
import form.User
import infrastructure.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/user") {
        post{
            try{
            val user = call.receive<CreateUserForm>()
            val id = userService.createResult(user)
            call.respond(HttpStatusCode.Created, id)
        } catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
    }
        }
    }
    }
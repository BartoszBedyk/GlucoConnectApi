package rest

import form.CreateUserForm
import form.CreateUserFormWithType
import form.UpdatePrefUnit
import form.UpdateUserNullForm
import infrastructure.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/user") {

        post {
            try {
                val user = call.receive<CreateUserForm>()
                val id = userService.createUser(user)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }
        post("/withType") {
            try {
                val user = call.receive<CreateUserFormWithType>()
                val id = userService.createUserWithType(user)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userService.getUser(id)
            call.respond(HttpStatusCode.OK, result)
        }

        put("/block/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userService.blockUser(id) ?: throw IllegalArgumentException("Invalid ID")
            call.respond(HttpStatusCode.OK, result)
        }

        put("/unblock/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userService.unblockUser(id) ?: throw IllegalArgumentException("Invalid ID")
            call.respond(HttpStatusCode.OK, result)
        }

        get("/all") {
            val result = userService.getAllUsers()
            call.respond(HttpStatusCode.OK, result)
        }

        put("/unitUpdate") {
            val form = call.receive<UpdatePrefUnit>()
            val result = userService.updateUnit(form)
            call.respond(HttpStatusCode.OK, result)
        }

        put("/updateNulls"){
            val form = call.receive<UpdateUserNullForm>()
            val result = userService.updateUserNulls(form)
            call.respond(HttpStatusCode.OK, result)
        }


    }
}
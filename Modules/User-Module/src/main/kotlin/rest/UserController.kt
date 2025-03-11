package rest

import form.*
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
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = userService.getUser(id)
                if (result != null) {
                    call.respond(HttpStatusCode.OK, result)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Internal server error")
            }
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

        get("/unit/{id}"){
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userService.getUserUnitById(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("observe/{partOne}/{partTwo}") {
            val partOne = call.parameters["partOne"] ?: throw IllegalArgumentException("Invalid Part One")
            val partTwo = call.parameters["partTwo"] ?: throw IllegalArgumentException("Invalid Part Two")

            try {
                val result = userService.observe(partOne, partTwo)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.localizedMessage ?: "Unknown error")))
            }
        }



    }
}
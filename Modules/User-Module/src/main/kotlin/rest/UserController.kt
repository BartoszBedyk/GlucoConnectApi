package rest

import form.UpdatePrefUnit
import form.UpdateUserNullForm
import infrastructure.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.userRoutes(userService: UserService) {
    route("/user") {
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

        put("/updateNulls") {
            val form = call.receive<UpdateUserNullForm>()
            val result = userService.updateUserNulls(form)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = userService.deleteUser(id)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        put("/{id}/{newPassword}/reset-password") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val newPassword = call.parameters["newPassword"] ?: throw IllegalArgumentException("Invalid Password")
                val result = userService.resetPassword(id, newPassword) ?: throw IllegalArgumentException("Invalid ID")
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        get("/unit/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userService.getUserUnitById(id)
            call.respond(HttpStatusCode.OK, result)
        }

        put("/{userId}/type/{userType}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val type = call.parameters["userType"] ?: throw IllegalArgumentException("Invalid Type")
            try {
                val result = userService.changeUserType(id, type)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        put("/{userId}/diabetes/{type}") {
            val id = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid ID")
            val type = call.parameters["type"] ?: throw IllegalArgumentException("Invalid Diabetes Type")
            try {
                val result = userService.changeUserDiabetes(id, type)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        put("/createUser/{userId}/type/{userType}") {
            val id = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid ID")
            val type = call.parameters["userType"] ?: throw IllegalArgumentException("Invalid Type")
            try {
                val result = userService.changeUserType(id, type)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
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

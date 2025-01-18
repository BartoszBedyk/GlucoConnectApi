package rest


import form.CreateUserMedication
import form.GetMedicationForm
import infrastructure.UserMedicationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userMedicationRoutes(userMedicationService: UserMedicationService) {
    route("/user-medications") {

        post {
            try {
                val userMedication = call.receive<CreateUserMedication>()
                val id = userMedicationService.createUserMedication(userMedication)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.readUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/user"){
            try {
                val getUserMedication = call.receive<GetMedicationForm>()
                val result = userMedicationService.readOneUserMedication(getUserMedication)
                println("______________________________________________________________-")
                println(getUserMedication)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                println("______________________________________________________________-")
                println(e.message)
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }

        }

        get("/today/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.readTodayUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.deleteUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/user/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.deleteUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }


    }
}
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

        get("/ID/{umId}") {
            try{
                val id = call.parameters["umId"] ?: throw IllegalArgumentException("Invalid ID")
                val result = userMedicationService.readUserMedicationByID(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e: Exception){
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }

        }

        get("/um/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.readUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/user/{userId}/{medicationId}") {
            try {
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing userId")
                val medicationId = call.parameters["medicationId"] ?: throw IllegalArgumentException("Missing medicationId")

                val getUserMedication = GetMedicationForm(userId, medicationId)

                val result = userMedicationService.readOneUserMedication(getUserMedication)

                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }


        get("/today/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.readTodayUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.deleteUserMedicationById(id)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/user/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.deleteUserMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/umById/{id}/{medicationId}"){
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val medicationId = call.parameters["medicationId"] ?: throw IllegalArgumentException("Invalid ID")
            val result = userMedicationService.getUserMedicationId(id,medicationId)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/history/{userId}"){
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid UserId")
            val result = userMedicationService.getUserMedicationHistory(userId)
            call.respond(HttpStatusCode.OK, result)
        }


        put("/{userId}/sync"){
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid UserId")
            val result = userMedicationService.markAsSynced(userId)
            call.respond(HttpStatusCode.OK, result)

        }





    }
}
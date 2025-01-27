package rest

import form.CreateMedication
import infrastructure.MedicationsService


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.medicationRoutes(medicationsService: MedicationsService) {
    route("/medications") {

        post{
            try {
                val medication = call.receive<CreateMedication>()
                val id = medicationsService.createMedication(medication)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        post("/more") {
            try {
                val medications = call.receive<List<CreateMedication>>()
                val ids = medicationsService.createMedications(medications)
                call.respond(HttpStatusCode.Created, ids)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }



        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = medicationsService.readMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }


        get("/all") {
            val result = medicationsService.getAll()
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = medicationsService.deleteMedication(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/{userId}/unsynced"){
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid UserId")
            val result = medicationsService.getUnsynced(userId)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
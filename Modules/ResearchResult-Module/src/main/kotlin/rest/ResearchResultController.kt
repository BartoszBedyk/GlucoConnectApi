package rest

import form.GlucoseResult
import form.ResearchResultForm
import form.SafeDeleteResultForm
import form.UpdateResearchResultForm
import infrastructure.ResearchResultService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.researchResultRoutes(researchService: ResearchResultService) {
    route("/results") {

        post {
            try {
                val result = call.receive<ResearchResultForm>()
                val id = researchService.createGlucoseResult(result)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        post("/sync") {
            try {
                val glucoseResultForm = call.receive<GlucoseResult>()
                val id = researchService.syncGlucoseResults(glucoseResultForm)
                call.respond(HttpStatusCode.OK, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = researchService.getGlucoseResultById(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/all") {
            val result = researchService.getAllResults()
            call.respond(HttpStatusCode.OK, result)
        }

        put("/update") {
            val parameters = call.receive<UpdateResearchResultForm>()
            val result = researchService.updateResult(parameters)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/three/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = researchService.getThreeResultsForId(id)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/all/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val result = researchService.getResultsByUserId(id)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/delete/{id}") {
            val id = call.parameters["id"].toString()
            researchService.deleteResult(id)
            call.respond(HttpStatusCode.OK)
        }

        put("/safeDelete") {
            val parameters = call.receive<SafeDeleteResultForm>()
            val result = researchService.safeDeleteResult(parameters)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/hb1ac/{userId}"){
            try{
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid UserId")
                val result = researchService.getUserGbA1cById(userId)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID: ${e.message}")
            }

        }
    }
}

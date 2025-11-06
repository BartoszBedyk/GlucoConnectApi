package rest

import form.HeartbeatForm
import infrastructure.HeartbeatResultService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route


fun Route.heartbeatRoutes(heartbeatResultService: HeartbeatResultService) {
    route("/heartbeat") {
        post{
            try{
                val heartbeat = call.receive<HeartbeatForm>()
                val id = heartbeatResultService.createResult(heartbeat)
                call.respond(HttpStatusCode.Created, id)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        get("/{id}"){
            try{
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = heartbeatResultService.readResultById(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }

        get("/user/{id}"){
            try{
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = heartbeatResultService.readResultByUserId(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }

        get("/three/{id}"){
            try{
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = heartbeatResultService.getThreeHeartbeatResults(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }

        delete("/{id}") {
            try{
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = heartbeatResultService.deleteHeartbeatResult(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }

        delete("/user/{id}") {
            try{
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = heartbeatResultService.deleteHeartbeatResultByUser(id)
                call.respond(HttpStatusCode.OK, result)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, e)
            }
        }
    }
}
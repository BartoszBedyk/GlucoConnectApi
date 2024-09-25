package rest

import form.CreteActivityForm
import infrastructure.ActivityService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.activityRoutes(activityService: ActivityService) {
    route("/activity") {
        post{
            try{
                val activity = call.receive<CreteActivityForm>()
                val id = activityService.createActivity(activity)
                call.respond(HttpStatusCode.Created, id)

            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val activity = activityService.getActivityById(id)
            call.respond(HttpStatusCode.OK, activity)
        }
    }
}
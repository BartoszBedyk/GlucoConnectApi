package rest

import form.CreteActivityForm
import infrastructure.ActivityService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.activityRoutes(activityService: ActivityService) {
    route("/activity") {
        post {
            try {
                val activity = call.receive<CreteActivityForm>()
                val id = activityService.createActivity(activity)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val activity = activityService.getActivityById(id)
            call.respond(HttpStatusCode.OK, activity)
        }

        get("/type/{type}") {
            val type = call.parameters["type"] ?: throw IllegalArgumentException("Invalid type")
            val activity = activityService.getActivityByType(type)
            call.respond(HttpStatusCode.OK, activity)
        }

        get("/user/{user}") {
            val user = call.parameters["user"] ?: throw IllegalArgumentException("Invalid user")
            val activity = activityService.getActivityByType(user)
            call.respond(HttpStatusCode.OK, activity)
        }
    }
}

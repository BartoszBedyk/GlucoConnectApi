package presentation

import domain.ActivityService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.CreateActivityRequest
import respondBadRequest
import respondNotFound

fun Route.activityController(activityService: ActivityService) {
    route("/activities") {

        post {
            val request = runCatching { call.receive<CreateActivityRequest>() }
                .getOrElse {
                    return@post call.respondBadRequest("Invalid request body")
                }

            val created = activityService.createActivity(request)
            call.respond(HttpStatusCode.Created, created)
        }

        get {
            val activities = activityService.getAllActivities()
            call.respond(HttpStatusCode.OK, activities)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Invalid or missing 'id' parameter")

            val activity = activityService.getActivityById(id)
                ?: return@get call.respondNotFound("Activity not found")

            call.respond(HttpStatusCode.OK, activity)
        }

        get("/user/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Invalid or missing 'id' parameter")

            val userActivities = activityService.getActivityByUserId(userId)
            call.respond(HttpStatusCode.OK, userActivities)
        }
    }
}

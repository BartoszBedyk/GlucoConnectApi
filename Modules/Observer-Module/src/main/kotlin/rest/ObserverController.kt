package rest

import form.CreateObserver
import infrastructure.ObserverDao
import infrastructure.ObserverService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route


fun Route.observerRoutes(observerService: ObserverService) {
    route("/observer") {

        post {
            val createObserver = call.receive<CreateObserver>()
            val observerId = observerService.observe(createObserver)
            call.respond(HttpStatusCode.Created, mapOf("observerId" to observerId))
        }

        get("/{observerId}/accepted") {
            val observerId = call.parameters["observerId"] ?: throw IllegalArgumentException("Invalid Part One")
            try{
                val observedList = observerService.getObservedAcceptedByObserverId(observerId)
                call.respond(HttpStatusCode.OK, observedList)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{observerId}/unaccepted") {
            val observerId = call.parameters["observerId"] ?: throw IllegalArgumentException("Invalid Part One")
            try{
                val observedList = observerService.getObservedUnAcceptedByObserverId(observerId)
                call.respond(HttpStatusCode.OK, observedList)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/pending/{observedId}"){
        val observedId = call.parameters["observedId"] ?: throw IllegalArgumentException("Invalid Part One")
            try{
                val observatorsUnAccepted = observerService.getObservatorsByObservedIdUnAccepted(observedId)
                call.respond(HttpStatusCode.OK, observatorsUnAccepted)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/accepted/{observedId}") {
            val observedId = call.parameters["observedId"] ?: throw IllegalArgumentException("Invalid Part One")
            println("Nie odpala sie!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            try{
                val observatorsAccepted = observerService.getObservatorsByObservedIdAccepted(observedId)
                call.respond(HttpStatusCode.OK, observatorsAccepted)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("/accept") {
            try {
                val createObserver = call.receive<CreateObserver>()
                val updatedRows = observerService.acceptObservation(createObserver)

                if (updatedRows > 0) {
                    call.respond(HttpStatusCode.OK, "Observation accepted")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Observation not found or already accepted")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error processing request: ${e.message}")
            }
        }

        put("/unaccept") {
            try {
                val createObserver = call.receive<CreateObserver>()
                val updatedRows = observerService.unAcceptObservation(createObserver)

                if (updatedRows > 0) {
                    call.respond(HttpStatusCode.OK, "Observation unaccepted")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Observation not found or already unaccepted")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error processing request: ${e.message}")
            }
        }

    }
}
package rest

import form.CreateObserver
import infrastructure.ObserverDao
import infrastructure.ObserverService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

        get("/{observedId}/declined") {
            val observedId = call.parameters["observedId"] ?: throw IllegalArgumentException("Invalid Part One")
            try{
                val observators = observerService.getObservatorsByObservedIdUnAccepted(observedId)
                call.respond(HttpStatusCode.OK, observators)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{observedId}/accepted") {
            val observedId = call.parameters["observedId"] ?: throw IllegalArgumentException("Invalid Part One")
            try{
                val observators = observerService.getObservatorsByObservedIdAccepted(observedId)
                call.respond(HttpStatusCode.OK, observators)
            }catch(e:Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("/accept") {
            val createObserver = call.receive<CreateObserver>()
            val updatedRows = observerService.acceptObservation(createObserver)
            if (updatedRows > 0) {
                call.respond(HttpStatusCode.OK, "Observation accepted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Observation not found or already accepted")
            }
        }

        put("/unaccept") {
            val createObserver = call.receive<CreateObserver>()
            val updatedRows = observerService.unAcceptObservation(createObserver)
            if (updatedRows > 0) {
                call.respond(HttpStatusCode.OK, "Observation unaccepted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Observation not found or already unaccepted")
            }
        }
    }
}
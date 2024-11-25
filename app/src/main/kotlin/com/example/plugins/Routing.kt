package com.example.plugins


import infrastructure.*
import infrastructure.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rest.*

import javax.sql.DataSource

fun Application.configureRouting(dataSource: DataSource) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val researchResultDao = ResearchResultDao(dataSource)
    val researchResultService = ResearchResultService(researchResultDao);

    val userDao = UserDao(dataSource)
    val userService = UserService(userDao)

    val activityDao = ActivityDao(dataSource)
    val activityService = ActivityService(activityDao)

    val heartbeatResultDao = HeartbeatResultDao(dataSource)
    val heartbeatService = HeartbeatResultService(heartbeatResultDao)

    val medicationDao = MedicationsDao(dataSource)
    val medicationService = MedicationsService(medicationDao)

    val userMedicationDao = UserMedicationDao(dataSource)
    val userMedicationService = UserMedicationService(userMedicationDao)

    routing {
        researchResultRoutes(researchResultService)
        userRoutes(userService)
        activityRoutes(activityService)
        heartbeatRoutes(heartbeatService)
        medicationRoutes(medicationService)
        userMedicationRoutes(userMedicationService)

        get("/") {
            call.respondText("Hello World!")
        }
    }
}

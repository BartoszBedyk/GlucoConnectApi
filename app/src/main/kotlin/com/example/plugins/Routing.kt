package com.example.plugins


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import form.UserCredentials
import infrastructure.*
import infrastructure.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rest.*
import java.util.*

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
        authenticate("auth-jwt") {
            researchResultRoutes(researchResultService)
            userRoutes(userService)
            activityRoutes(activityService)
            heartbeatRoutes(heartbeatService)
            medicationRoutes(medicationService)
            userMedicationRoutes(userMedicationService)
        }

        get("/") {
            call.respondText("Hello World!")
        }

        post("/login") {
            val credentials = call.receive<UserCredentials>()
            val user = userService.authenticate(credentials)

            if (user != null) {
                val token = JWT.create()
                    .withAudience("myaudience")
                    .withIssuer("myissuer")
                    .withClaim("username", user.email)
                    .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                    .sign(Algorithm.HMAC256("secret"))

                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }

}


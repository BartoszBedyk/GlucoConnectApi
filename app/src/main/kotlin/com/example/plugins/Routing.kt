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
                    .withClaim("userId", user.id.toString())
                    .withClaim("username", user.email)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256("secret"))

                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        post("/refresh-token") {
            val currentToken = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (currentToken == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing token")
                return@post
            }

            try {
                val verifier = JWT.require(Algorithm.HMAC256("secret"))
                    .withAudience("myaudience")
                    .withIssuer("myissuer")
                    .build()

                val decodedJWT = verifier.verify(currentToken)
                val now = Date()
                val expiration = decodedJWT.expiresAt

                if (expiration == null || now.after(expiration)) {
                    call.respond(HttpStatusCode.Unauthorized, "Token expired")
                    return@post
                }


                val refreshThreshold = 24 * 60 * 60 * 1000
                val timeToExpiration = expiration.time - now.time

                if (timeToExpiration > refreshThreshold) {
                    call.respond(HttpStatusCode.BadRequest, "Token is still valid and not close to expiration")
                    return@post
                }

                val newToken = JWT.create()
                    .withAudience("myaudience")
                    .withIssuer("myissuer")
                    .withClaim("userId", decodedJWT.getClaim("userId").asString())
                    .withClaim("username", decodedJWT.getClaim("username").asString())
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256("secret"))

                call.respond(mapOf("token" to newToken))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            }
        }

    }


}


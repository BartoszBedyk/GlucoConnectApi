package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.reporting.reportRoutes
import com.example.reporting.services.ThymeleafTemplateRenderer
import form.CreateUserFormWithType
import form.CreateUserStepOneForm
import form.CreateUserStepTwoForm
import form.UserCredentials
import hashPassword
import infrastructure.ActivityDao
import infrastructure.ActivityService
import infrastructure.HeartbeatResultDao
import infrastructure.HeartbeatResultService
import infrastructure.MedicationsDao
import infrastructure.MedicationsService
import infrastructure.ObserverDao
import infrastructure.ObserverService
import infrastructure.ResearchResultDao
import infrastructure.ResearchResultService
import infrastructure.UserDao
import infrastructure.UserMedicationDao
import infrastructure.UserMedicationService
import infrastructure.UserService
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import loadSecretKey
import rest.activityRoutes
import rest.heartbeatRoutes
import rest.medicationRoutes
import rest.observerRoutes
import rest.researchResultRoutes
import rest.userMedicationRoutes
import rest.userRoutes
import java.util.Date
import javax.sql.DataSource

fun Application.configureRouting(dataSource: DataSource) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Internal error: ${cause.message}")
        }
    }

    val dotenv = dotenv()

    val base64Key = dotenv["ENCRYPTION_KEY"]
    val encryptionKey = loadSecretKey(base64Key)

    val researchResultDao = ResearchResultDao(dataSource)
    val researchResultService = ResearchResultService(researchResultDao, encryptionKey)

    val userDao = UserDao(dataSource)
    val userService = UserService(userDao, encryptionKey)

    val activityDao = ActivityDao(dataSource)
    val activityService = ActivityService(activityDao)

    val heartbeatResultDao = HeartbeatResultDao(dataSource)
    val heartbeatService = HeartbeatResultService(heartbeatResultDao, encryptionKey)

    val medicationDao = MedicationsDao(dataSource)
    val medicationService = MedicationsService(medicationDao)

    val userMedicationDao = UserMedicationDao(dataSource)
    val userMedicationService = UserMedicationService(userMedicationDao, encryptionKey)

    val observerDao = ObserverDao(dataSource)
    val observerService = ObserverService(observerDao)

    val thymeleafTemplateRenderer = ThymeleafTemplateRenderer()

    val secretKey = dotenv["SECRET_KEY"]
    val audience = dotenv["AUDIENCE"]
    val issuer = dotenv["ISSUER"]

    fun String.hexStringToByteArray(): ByteArray {
        val len = this.length
        require(len % 2 == 0) { "Hex string must have an even length" }

        val result = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            val high = this[i].digitToIntOrNull(16) ?: error("Invalid hex character: ${this[i]}")
            val low = this[i + 1].digitToIntOrNull(16) ?: error("Invalid hex character: ${this[i + 1]}")
            result[i / 2] = ((high shl 4) + low).toByte()
        }
        return result
    }
    routing {
        @Serializable
        data class CreatedUserResponse(val id: String)

        post("/createUserStepOne") {
            try {
                val form = call.receive<CreateUserStepOneForm>()
                val hashedForm = CreateUserStepOneForm(form.email, hashPassword(form.password))
                val id = userService.createUser(hashedForm)
                call.respond(HttpStatusCode.Created, CreatedUserResponse(id.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        put("/createUserStepTwo") {
            val form = call.receive<CreateUserStepTwoForm>()
            try {
                val result = userService.createUserStepTwo(form)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        post("/createUser/withType") {
            try {
                val user = call.receive<CreateUserFormWithType>()
                val hashedForm = CreateUserFormWithType(user.email, hashPassword(user.password), user.userType)
                val id = userService.createUserWithType(hashedForm)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        post("/refresh-token") {
            val currentToken = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (currentToken == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing token")
                return@post
            }

            try {
                val verifier =
                    JWT.require(Algorithm.HMAC256(secretKey)).withAudience(audience).withIssuer(issuer).build()

                val decodedJWT = verifier.verify(currentToken)
                val now = Date()
                val expiration = decodedJWT.expiresAt

                if (expiration == null || now.after(expiration)) {
                    call.respond(HttpStatusCode.Unauthorized, "Token expired")
                    return@post
                }

                val refreshThreshold = 7L * 24 * 60 * 60 * 1000
                val timeToExpiration = expiration.time - now.time

                if (timeToExpiration > refreshThreshold) {
                    call.respond(HttpStatusCode.BadRequest, "Token is still valid and not close to expiration")
                    return@post
                }

                val newToken = JWT.create().withAudience(audience).withIssuer(issuer)
                    .withClaim("userId", decodedJWT.getClaim("userId").asString())
                    .withClaim("username", decodedJWT.getClaim("username").asString())
                    .withClaim("userType", decodedJWT.getClaim("userType").asString())
                    .withExpiresAt(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256(secretKey))

                call.respond(mapOf("token" to newToken))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            }
        }

        get("/") {
            call.respondText("Hello World!")
        }

        post("/login") {
            val credentials = call.receive<UserCredentials>()
            val hashedForm = UserCredentials(credentials.email, credentials.password)
            val user = userService.authenticate(hashedForm)

            if (user != null) {
                val token = JWT.create().withAudience(audience).withIssuer(issuer)
                    .withClaim("userId", user.id.toString()).withClaim("username", user.email)
                    .withClaim("userType", user.type.toString())
                    .withExpiresAt(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256(secretKey))

                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        get("/health") {
            println("HEALTH REQUEST from: ${call.request.origin.remoteHost}")
            call.respondText("API is healthy")
        }

        authenticate("auth-jwt") {
            researchResultRoutes(researchResultService)
            userRoutes(userService)
            activityRoutes(activityService)
            heartbeatRoutes(heartbeatService)
            medicationRoutes(medicationService)
            userMedicationRoutes(userMedicationService)
            observerRoutes(observerService)
            reportRoutes(userService, researchResultService, thymeleafTemplateRenderer)
        }
    }
}

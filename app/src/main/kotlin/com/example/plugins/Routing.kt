package com.example.plugins


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.documentGenerator.DocumentService.ThymeleafTemplateRenderer
import com.example.documentGenerator.reportRoutes
import form.CreateUserForm
import form.CreateUserFormWithType
import form.UpdateUserNullForm
import form.UserCredentials
import hashPassword
import infrastructure.*
import infrastructure.UserService
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import rest.*
import java.util.*
import javax.sql.DataSource

fun Application.configureRouting(dataSource: DataSource) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Internal error: ${cause.message}")
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

    val observerDao = ObserverDao(dataSource)
    val observerService = ObserverService(observerDao)

    val thymeleafTemplateRenderer = ThymeleafTemplateRenderer()

    val dotenv = dotenv()
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

        post("/createUser") {
            try {
                val form = call.receive<CreateUserForm>()
                val hashedForm = CreateUserForm(form.email,hashPassword(form.password))
                val id = userService.createUser(hashedForm)
                call.respond(HttpStatusCode.Created, CreatedUserResponse(id.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        post("/createUser/withType") {
            try {
                val user = call.receive<CreateUserFormWithType>()
                val hashedForm = CreateUserFormWithType(user.email,hashPassword(user.password), user.userType)
                val id = userService.createUserWithType(hashedForm)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        put("/createUser/{userId}/type/{userType}") {
            val id = call.parameters["userId"] ?: throw IllegalArgumentException("Invalid ID")
            val type = call.parameters["userType"] ?: throw IllegalArgumentException("Invalid Type")
            try {
                val result = userService.changeUserType(id, type)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }


        put("/createUser/updateNulls") {
            val form = call.receive<UpdateUserNullForm>()
            try {
                val result = userService.updateUserNulls(form)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
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
            call.respond(HttpStatusCode.OK, "API is healthy")
        }


        authenticate("auth-jwt") {
            researchResultRoutes(researchResultService)
            userRoutes(userService)
            activityRoutes(activityService)
            heartbeatRoutes(heartbeatService)
            medicationRoutes(medicationService)
            userMedicationRoutes(userMedicationService)
            observerRoutes(observerService)
            reportRoutes(userService, researchResultService, thymeleafTemplateRenderer )
        }


    }


}


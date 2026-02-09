package com.example.plugins

import infrastructure.MedicationsDao
import infrastructure.MedicationsService
import infrastructure.ObserverDao
import infrastructure.ObserverService
import infrastructure.UserMedicationDao
import infrastructure.UserMedicationService
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.origin
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import javax.sql.DataSource
import loadSecretKey
import rest.medicationRoutes
import rest.observerRoutes
import rest.userMedicationRoutes

// detekt:disable LongMethod
@Suppress("MagicNumber", "LongMethod")
fun Application.configureRouting(dataSource: DataSource) {
    val dotenv = dotenv()

    val base64Key = dotenv["ENCRYPTION_KEY"]
    val encryptionKey = loadSecretKey(base64Key)


    val medicationDao = MedicationsDao(dataSource)
    val medicationService = MedicationsService(medicationDao)

    val userMedicationDao = UserMedicationDao(dataSource)
    val userMedicationService = UserMedicationService(userMedicationDao, encryptionKey)

    val observerDao = ObserverDao(dataSource)
    val observerService = ObserverService(observerDao)


    routing {

        get("/") {
            call.respondText("Hello World!")
        }


        get("/health") {
            println("HEALTH REQUEST from: ${call.request.origin.remoteHost}")
            call.respondText("API is healthy")
        }

        authenticate("auth-jwt") {
            medicationRoutes(medicationService)
            userMedicationRoutes(userMedicationService)
            observerRoutes(observerService)
        }
    }
}
// detekt:enable LongMethod

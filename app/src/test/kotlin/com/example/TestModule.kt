package com.example

import UserPrincipal
import com.example.plugins.configureSerialization
import data.GlucoseRepository
import data.HeartbeatRepository
import domain.GlucoseService
import domain.HeartbeatService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.routing.routing
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import presentation.glucoseController
import presentation.heartbeatController
import java.util.UUID

fun Application.testModule() {
    install(Koin) {
        modules(
            module {
                single { GlucoseRepository() }
                single { GlucoseService(get()) }
            },
            module {
                single { HeartbeatRepository() }
                single { HeartbeatService(get()) }
            }
        )
    }

    install(Authentication) {
        basic("test-auth") {
            validate { credentials ->
                if (credentials.name == "test" && credentials.password == "test") {
                    UserPrincipal(
                        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        userType = "USER"
                    )
                } else {
                    null
                }
            }
        }
    }

    configureSerialization()

    val glucoseService by inject<GlucoseService>()
    val heartbeatService by inject<HeartbeatService>()

    routing {
        authenticate("test-auth") {
            glucoseController(glucoseService)
            heartbeatController(heartbeatService)
        }
    }
}

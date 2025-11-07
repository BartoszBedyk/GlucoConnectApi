package com.example.plugins

import di.activityModule
import di.glucoseModule
import domain.ActivityService
import domain.GlucoseService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import presentation.activityController
import presentation.glucoseController

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            activityModule,
            glucoseModule
        )
    }

    val activityService by inject<ActivityService>()
    val glucoseService by inject<GlucoseService>()

    routing {
        activityController(activityService)
        glucoseController(glucoseService)
    }
}

package com.example.plugins

import di.activityModule
import di.glucoseModule
import di.userModule
import domain.ActivityService
import domain.GlucoseService
import domain.UserService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import presentation.activityController
import presentation.glucoseController
import presentation.userController

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            activityModule,
            glucoseModule,
            userModule
        )
    }

    val activityService by inject<ActivityService>()
    val glucoseService by inject<GlucoseService>()
    val userService by inject<UserService>()

    routing {
        activityController(activityService)
        glucoseController(glucoseService)
        userController(userService)
    }
}

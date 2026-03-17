package com.example.plugins

import JwtHelper
import di.activityModule
import di.authenticationModule
import di.drugModule
import di.glucoseModule
import di.heartbeatModule
import di.observerModule
import di.userModule
import domain.ActivityService
import domain.AuthenticationService
import domain.DrugService
import domain.GlucoseService
import domain.HeartbeatService
import domain.ObserverService
import domain.UserService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import presentation.activityController
import presentation.authenticationController
import presentation.drugController
import presentation.glucoseController
import presentation.heartbeatController
import presentation.observerController
import presentation.userController

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(
            activityModule,
            glucoseModule,
            heartbeatModule,
            userModule,
            authenticationModule,
            observerModule,
            drugModule
        )
    }

    val activityService by inject<ActivityService>()
    val glucoseService by inject<GlucoseService>()
    val userService by inject<UserService>()
    val heartbeatService by inject<HeartbeatService>()
    val observerService by inject<ObserverService>()
    val authenticationService by inject<AuthenticationService>()
    val drugService by inject<DrugService>()
    val jwtHelper by inject<JwtHelper>()

    routing {
        authenticate("auth-jwt") {
            activityController(activityService)
            glucoseController(glucoseService)
            userController(userService)
            heartbeatController(heartbeatService)
            observerController(observerService)
            drugController(drugService)
        }
        authenticationController(authenticationService, jwtHelper)
    }
}

package presentation

import domain.AuthenticationService
import domain.JwtHelper
import io.ktor.server.routing.Route

fun Route.authenticationController(authenticationService: AuthenticationService, jwtHelper: JwtHelper) {
}

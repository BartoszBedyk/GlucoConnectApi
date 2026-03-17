package presentation

import JwtHelper
import domain.AuthenticationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import model.AuthenticationCredentials
import respondValidationError

fun Route.authenticationController(authenticationService: AuthenticationService, jwtHelper: JwtHelper) {
    route("/auth") {
        post("/login") {
            val credentials = runCatching { call.receive<AuthenticationCredentials>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val token = authenticationService.loginUser(credentials)
            call.respond(HttpStatusCode.OK, token)
        }

        post("/register") {
            val credentials = runCatching { call.receive<AuthenticationCredentials>() }
                .getOrElse {
                    return@post call.respondValidationError("Invalid JSON body or missing fields")
                }

            val token = authenticationService.registerUser(credentials)
            call.respond(HttpStatusCode.OK, token)
        }

        post("/refresh") {
            val authHeader = call.request.headers["Authorization"]
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing Authorization header")

            val currentToken = authHeader.removePrefix("Bearer ").trim()
            if (currentToken.isEmpty()) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid token format")
            }

            val newToken = authenticationService.refreshUserToken(currentToken)
            call.respond(HttpStatusCode.OK, mapOf("token" to newToken))
        }
    }
}

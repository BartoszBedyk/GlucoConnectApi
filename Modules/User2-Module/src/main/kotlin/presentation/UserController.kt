package presentation

import domain.UserService
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.userController(userService: UserService) {
    route("/user") {

    }
}

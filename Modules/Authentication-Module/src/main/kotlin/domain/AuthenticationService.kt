package domain

import data.AuthenticationRepository
import io.ktor.server.plugins.BadRequestException
import model.AuthenticationCredentials

class AuthenticationService(private val authenticationRepository: AuthenticationRepository): JwtHelper() {

    fun loginUser(authData: AuthenticationCredentials): String {
        val loggedUser = authenticationRepository.login(authData)
            ?: throw BadRequestException("Invalid login credentials")

        return createToken(loggedUser)
    }

     fun refreshUserToken(token: String): String {
        return refreshToken(token)
    }

}

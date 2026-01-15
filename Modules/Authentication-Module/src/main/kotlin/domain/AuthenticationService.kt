package domain

import JwtHelper
import data.AuthenticationRepository
import hashPassword
import io.ktor.server.plugins.BadRequestException
import model.AuthenticationCredentials

class AuthenticationService(private val authenticationRepository: AuthenticationRepository): JwtHelper() {

    fun loginUser(authData: AuthenticationCredentials): String {
        val loggedUser = authenticationRepository.login(authData)
            ?: throw BadRequestException("Invalid login credentials")

        return createToken(loggedUser)
    }

    fun registerUser(authData: AuthenticationCredentials): String {
        AuthenticationCredentials(authData.username, hashPassword(authData.password)).let {
            val auth = authenticationRepository.create(it)
            return createToken(auth)
        }
    }

     fun refreshUserToken(token: String): String {
        return refreshToken(token)
    }



}

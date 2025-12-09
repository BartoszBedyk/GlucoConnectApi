package domain

import data.AuthenticationRepository
import model.AuthenticationCredentials

class AuthenticationService(private val authenticationRepository: AuthenticationRepository) {

    fun loginUser(authData: AuthenticationCredentials) = authenticationRepository.login(authData)
}

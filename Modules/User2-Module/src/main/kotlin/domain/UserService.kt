package domain

import data.UserRepository
import model.CreateUserRequest
import java.util.UUID

class UserService(private val userRepository: UserRepository) {
    fun createUser(request: CreateUserRequest) = userRepository.createUser(request)

    fun getUserById(id: UUID) = userRepository.findUserById(id)
}

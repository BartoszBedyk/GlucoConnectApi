package domain

import data.UserRepository
import model.CreateUserRequest
import java.util.UUID

class UserService(private val userRepository: UserRepository) {
    fun createUser(request: CreateUserRequest, userId: UUID) = userRepository.createUser(request, userId)

    fun getUserById(id: UUID) = userRepository.findUserById(id)
}

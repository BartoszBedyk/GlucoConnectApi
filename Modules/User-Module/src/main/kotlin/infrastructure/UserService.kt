package infrastructure

import form.CreateUserForm
import form.User
import java.util.*

class UserService(private val userDao: UserDao) {

    suspend fun createResult(form: CreateUserForm): UUID {
        return userDao.createUser(form)
    }

    suspend fun blockUser(id: String): Int {
        return userDao.blockUser(id.toString())
    }

    suspend fun unblockUser(id: String): Int {
        return userDao.unblockUser(id.toString())
    }

    suspend fun getUser(id: String): User {
        return userDao.readUser(id.toString())
    }

}
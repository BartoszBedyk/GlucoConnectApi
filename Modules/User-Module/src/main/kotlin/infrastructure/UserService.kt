package infrastructure

import form.CreateUserForm
import java.util.*

class UserService(private val userDao: UserDao) {

    suspend fun createResult(form: CreateUserForm): UUID {
        return userDao.createUser(form)
    }

}
package infrastructure

import form.*
import java.util.*

class UserService(private val userDao: UserDao) {

    suspend fun createUser(form: CreateUserForm): UUID {
        return userDao.createUser(form)
    }

    suspend fun createUserWithType(form: CreateUserFormWithType): UUID {
        return userDao.createUserWithType(form)
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

    suspend fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    suspend fun updateUnit(form: UpdatePrefUnit): Int{
        return userDao.updateUnit(form)
    }

    suspend fun updateUserNulls(form: UpdateUserNullForm) : Int{
        return userDao.updateUserNulls(form)
    }

    suspend fun authenticate(form: UserCredentials): User? {
        return userDao.authenticate(form)
    }

    suspend fun getUserUnitById(id: String): PrefUnitType {
        return userDao.getUserUnitById(id)
    }

    suspend fun observe(partOne: String, partTwo: String): User {
        return userDao.observe(partOne, partTwo)
    }



}
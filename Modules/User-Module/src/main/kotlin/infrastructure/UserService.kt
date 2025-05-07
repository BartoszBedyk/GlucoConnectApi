package infrastructure

import form.*
import java.util.*

class UserService(private val userDao: UserDao) {



    suspend fun createUser(form: CreateUserForm): UUID {
        return userDao.createUser(form)
    }

    //Creates user with additional data for instance preferred glucose unit type, first and last name
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

    //Returns All users data but without password
    suspend fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    suspend fun updateUnit(form: UpdatePrefUnit): Int{
        return userDao.updateUnit(form)
    }

    //Update User data same as createUserWithType
    suspend fun updateUserNulls(form: UpdateUserNullForm) : Int{
        return userDao.updateUserNulls(form)
    }

    suspend fun authenticate(form: UserCredentials): User? {
        return userDao.authenticate(form)
    }

    suspend fun changeUserType(id:String, type: String){
         userDao.changeUserType(id, type)
    }

    suspend fun changeUserDiabetes(id:String, type:String){
        userDao.changeUserDiabetesType(id,type)
    }

    suspend fun getUserUnitById(id: String): PrefUnitType {
        return userDao.getUserUnitById(id)
    }

    suspend fun observe(partOne: String, partTwo: String): User {
        return userDao.observe(partOne, partTwo)
    }

    suspend fun deleteUser(userId: String){
        userDao.deleteUser(userId)
    }

    suspend fun resetPassword(userId: String, newPassword: String) {
        userDao.resetPassword(userId, newPassword)
    }



}
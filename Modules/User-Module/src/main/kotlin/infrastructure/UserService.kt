package infrastructure

import form.*
import verifyPassword
import java.util.*
import javax.crypto.SecretKey

class UserService(private val userDao: UserDao, private val secretKey: SecretKey) {



    suspend fun createUser(form: CreateUserStepOneForm): UUID {
        return userDao.createUserStepOne(form,secretKey)
    }

    suspend fun createUserStepTwo(form: CreateUserStepTwoForm){
        userDao.createUserStepTwo(form,secretKey)
    }

    //Creates user with additional data for instance preferred glucose unit type, first and last name
    suspend fun createUserWithType(form: CreateUserFormWithType): UUID {
        return userDao.createUserWithType(form, secretKey)
    }

    suspend fun blockUser(id: String): Int {
        return userDao.blockUser(id.toString())
    }

    suspend fun unblockUser(id: String): Int {
        return userDao.unblockUser(id.toString())
    }

    suspend fun getUser(id: String): User {
        return userDao.readUser(id, secretKey)
    }

    //Returns All users data but without password
    suspend fun getAllUsers(): List<User> {
        return userDao.getAll(secretKey)
    }

    suspend fun updateUnit(form: UpdatePrefUnit): Int{
        return userDao.updateUnit(form)
    }

    //Update User data same as createUserWithType
    suspend fun updateUserNulls(form: UpdateUserNullForm) : Int{
        return userDao.updateUserNulls(form, secretKey)
    }

    suspend fun authenticate(form: UserCredentials): User? {
        return if(verifyPassword(form.password, userDao.authenticateHash(form))){
            userDao.authenticate(form,secretKey)
        }else
            null
    }

    suspend fun changeUserType(id:String, type: String){
         userDao.changeUserType(id, type)
    }

    suspend fun changeUserDiabetes(id:String, type:String){
        userDao.changeUserDiabetesType(id,type, secretKey)
    }

    suspend fun getUserUnitById(id: String): PrefUnitType {
        return userDao.getUserUnitById(id)
    }

    suspend fun observe(partOne: String, partTwo: String): User {
        return userDao.observe(partOne, partTwo, secretKey)
    }

    suspend fun deleteUser(userId: String){
        userDao.deleteUser(userId)
    }

    suspend fun resetPassword(userId: String, newPassword: String) {
        userDao.resetPassword(userId, newPassword)
    }



}
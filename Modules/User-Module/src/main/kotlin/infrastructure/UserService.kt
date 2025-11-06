package infrastructure


import form.CreateUserFormWithType
import form.CreateUserStepOneForm
import form.CreateUserStepTwoForm
import form.PrefUnitType
import form.UpdatePrefUnit
import form.UpdateUserNullForm
import form.User
import form.UserCredentials
import java.util.UUID
import verifyPassword

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
        return userDao.getUserById(id, secretKey)
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
        return userDao.updateUserProfileData(form, secretKey)
    }

    suspend fun authenticate(form: UserCredentials): User? {
        return if(verifyPassword(form.password, userDao.authenticateHash(form))){
            userDao.authenticate(form,secretKey)
        }else
            null
    }

    suspend fun changeUserType(id:String, type: String){
         userDao.updateUserType(id, type)
    }

    suspend fun changeUserDiabetes(id:String, type:String){
        userDao.updateUserDiabetesType(id,type, secretKey)
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
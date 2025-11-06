package infrastructure

import form.CreateUserFormWithType
import form.CreateUserStepOneForm
import form.CreateUserStepTwoForm
import form.PrefUnitType
import form.UpdatePrefUnit
import form.UpdateUserNullForm
import form.User
import form.UserCredentials
import verifyPassword
import java.util.UUID
import javax.crypto.SecretKey

class UserService(private val userDao: UserDao, private val secretKey: SecretKey) {

    suspend fun createUser(form: CreateUserStepOneForm): UUID = userDao.createUserStepOne(form, secretKey)

    suspend fun createUserStepTwo(form: CreateUserStepTwoForm) {
        userDao.createUserStepTwo(form, secretKey)
    }

    // Creates user with additional data for instance preferred glucose unit type, first and last name
    suspend fun createUserWithType(form: CreateUserFormWithType): UUID = userDao.createUserWithType(form, secretKey)

    suspend fun blockUser(id: String): Int = userDao.blockUser(id.toString())

    suspend fun unblockUser(id: String): Int = userDao.unblockUser(id.toString())

    suspend fun getUser(id: String): User = userDao.getUserById(id, secretKey)

    // Returns All users data but without password
    suspend fun getAllUsers(): List<User> = userDao.getAll(secretKey)

    suspend fun updateUnit(form: UpdatePrefUnit): Int = userDao.updateUnit(form)

    // Update User data same as createUserWithType
    suspend fun updateUserNulls(form: UpdateUserNullForm): Int = userDao.updateUserProfileData(form, secretKey)

    suspend fun authenticate(form: UserCredentials): User? =
        if (verifyPassword(form.password, userDao.authenticateHash(form))) {
            userDao.authenticate(form, secretKey)
        } else {
            null
        }

    suspend fun changeUserType(id: String, type: String) {
        userDao.updateUserType(id, type)
    }

    suspend fun changeUserDiabetes(id: String, type: String) {
        userDao.updateUserDiabetesType(id, type, secretKey)
    }

    suspend fun getUserUnitById(id: String): PrefUnitType = userDao.getUserUnitById(id)

    suspend fun observe(partOne: String, partTwo: String): User = userDao.observe(partOne, partTwo, secretKey)

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    suspend fun resetPassword(userId: String, newPassword: String) {
        userDao.resetPassword(userId, newPassword)
    }
}

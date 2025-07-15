package infrastructure

import form.*
import java.util.*
import javax.crypto.SecretKey

class UserMedicationService(private val userMedicationDao: UserMedicationDao, private val secretKey: SecretKey) {
    suspend fun createUserMedication(form: CreateUserMedication): UUID {
        return userMedicationDao.createUserMedication(form, secretKey)
    }

    suspend fun readUserMedication(id: String): List<UserMedication> {
        return userMedicationDao.getUserMedicationByUserId(id, secretKey)
    }

    suspend fun readUserMedicationByID(id: String): UserMedication? {
        return userMedicationDao.getUserMedicationById(id, secretKey);
    }

    suspend fun readOneUserMedication(form: GetMedicationForm): UserMedication {
        return userMedicationDao.getUserMedicationByUmAndUserIds(form, secretKey)
    }

    suspend fun readTodayUserMedication(id: String): List<UserMedication>{
        return userMedicationDao.getTodayUserMedicationByUserId(id, secretKey)
    }

    suspend fun deleteUserMedication(id: String) {
        userMedicationDao.deleteUserMedicationByUserId(id)
    }

    suspend fun deleteUserMedicationById(id: String) {
        userMedicationDao.deleteUserMedicationById(id)
    }

    suspend fun getUserMedicationId(id: String, medicationId: String): UUID{
        return UUID.fromString(userMedicationDao.getUserMedicationId(id,medicationId))
    }

    suspend fun getUserMedicationHistory(id: String): List<UserMedication> {
        return userMedicationDao.getUserMedicationHistory(id, secretKey)
    }

    suspend fun markAsSynced(userId: String) {
        userMedicationDao.markAsSynced(userId)
    }
}
package infrastructure

import form.*
import java.util.*

class UserMedicationService(private val userMedicationDao: UserMedicationDao) {
    suspend fun createUserMedication(form: CreateUserMedication): UUID {
        return userMedicationDao.createUserMedication(form)
    }

    suspend fun readUserMedication(id: String): List<UserMedication> {
        return userMedicationDao.readUserMedication(id)
    }

    suspend fun readUserMedicationByID(id: String): UserMedication? {
        return userMedicationDao.readUserMedicationByID(id);
    }

    suspend fun readOneUserMedication(form: GetMedicationForm): UserMedication {
        return userMedicationDao.readOneMedication(form)
    }

    suspend fun readTodayUserMedication(id: String): List<UserMedication>{
        return userMedicationDao.readTodayUserMedication(id)
    }

    suspend fun deleteUserMedication(id: String) {
        userMedicationDao.deleteUserMedication(id)
    }

    suspend fun deleteUserMedicationById(id: String) {
        userMedicationDao.deleteUserMedicationById(id)
    }

    suspend fun getUserMedicationId(id: String, medicationId: String): UUID{
        return UUID.fromString(userMedicationDao.getUserMedicationId(id,medicationId))
    }

    suspend fun getUserMedicationHistory(id: String): List<UserMedication> {
        return userMedicationDao.getUserMedicationHistory(id)
    }

    suspend fun markAsSynced(userId: String) {
        userMedicationDao.markAsSynced(userId)
    }
}
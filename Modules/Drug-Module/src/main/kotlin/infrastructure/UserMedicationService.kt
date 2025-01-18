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
        userMedicationDao.deleteUserMedication(id)
    }
}
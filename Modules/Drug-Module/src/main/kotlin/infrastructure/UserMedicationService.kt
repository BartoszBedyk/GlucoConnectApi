package infrastructure

import form.CreateMedication
import form.CreateUserMedication
import form.Medication
import form.UserMedication
import java.util.*

class UserMedicationService(private val userMedicationDao: UserMedicationDao) {
    suspend fun createUserMedication(form: CreateUserMedication): UUID {
        return userMedicationDao.createUserMedication(form)
    }

    suspend fun readUserMedication(id: String): List<UserMedication> {
        return userMedicationDao.readUserMedication(id)
    }

    suspend fun deleteUserMedication(id: String) {
        userMedicationDao.deleteUserMedication(id)
    }

    suspend fun deleteUserMedicationById(id: String) {
        userMedicationDao.deleteUserMedication(id)
    }
}
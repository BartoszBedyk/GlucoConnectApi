package infrastructure

import form.CreateMedication
import form.Medication
import java.util.*

class MedicationsService(private val medicationsDao: MedicationsDao) {
    suspend fun createMedication(form: CreateMedication): UUID {
        return medicationsDao.createMedication(form)
    }

    suspend fun createMedications(forms: List<CreateMedication>): List<UUID> {
        return medicationsDao.createMedications(forms)
    }

    suspend fun readMedication(id: String): Medication {
        return medicationsDao.getMedicationById(id)
    }

    suspend fun getAll(): MutableList<Medication> {
        return medicationsDao.getAll()
    }

    suspend fun deleteMedication(id: String) {
        medicationsDao.deleteMedication(id)
    }

    suspend fun getUnsynced(userId: String): MutableList<Medication> {
        return medicationsDao.syncMedication(userId)
    }
}
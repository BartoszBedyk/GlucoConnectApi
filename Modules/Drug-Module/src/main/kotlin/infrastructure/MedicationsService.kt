package infrastructure

import form.CreateMedication
import form.Medication
import java.util.UUID

class MedicationsService(private val medicationsDao: MedicationsDao) {
    suspend fun createMedication(form: CreateMedication): UUID = medicationsDao.createMedication(form)

    suspend fun createMedications(forms: List<CreateMedication>): List<UUID> = medicationsDao.createMedications(forms)

    suspend fun readMedication(id: String): Medication = medicationsDao.getMedicationById(id)

    suspend fun getAll(): MutableList<Medication> = medicationsDao.getAll()

    suspend fun deleteMedication(id: String) {
        medicationsDao.deleteMedication(id)
    }

    suspend fun getUnsynced(userId: String): MutableList<Medication> = medicationsDao.syncMedication(userId)
}

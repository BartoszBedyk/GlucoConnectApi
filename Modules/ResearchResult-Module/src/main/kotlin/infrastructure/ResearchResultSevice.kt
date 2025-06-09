package infrastructure


import form.GlucoseResult
import form.ResearchResultForm
import form.SafeDeleteResultForm
import form.UpdateResearchResultForm
import kotlinx.coroutines.runBlocking
import java.util.*

class ResearchResultService(private val researchResultDao: ResearchResultDao) {

    suspend fun createGlucoseResult(form: ResearchResultForm): UUID {
        validateForm(form)
        return researchResultDao.create(form)
    }

    suspend fun syncGlucoseResults(result: GlucoseResult): GlucoseResult {
        return researchResultDao.sync(result)
    }

    suspend fun getGlucoseResultById(id: String): GlucoseResult {
        return researchResultDao.getGlucoseResultById(id)
    }

    suspend fun getAllResults(): List<GlucoseResult> {
        return researchResultDao.getAll()
    }


    suspend fun getThreeResultsForId(id: String): List<GlucoseResult> {
        return researchResultDao.getThreeResultsForUser(id)
    }

    suspend fun getResultsByUserId(id: String): List<GlucoseResult> {
        return researchResultDao.getResultsByUserId(id)
    }

    suspend fun updateResult(form: UpdateResearchResultForm) {
        validateUpdateForm(form)
        researchResultDao.updateResult(form)
    }

    suspend fun deleteResult(id: String) {
        researchResultDao.deleteResult(id)
    }

    suspend fun safeDeleteResult(form: SafeDeleteResultForm) {
        researchResultDao.safeDeleteResult(form)
    }

    private fun validateForm(form: ResearchResultForm) {
        require(form.glucoseConcentration > 0) { "Glucose concentration must be greater than 0" }
    }

    private fun validateUpdateForm(form: UpdateResearchResultForm) {
        require(form.glucoseConcentration > 0) { "Glucose concentration must be greater than 0" }
    }

    suspend fun getUserGbA1cById(id: String): Double {
        return calculateGbA1c(id)
    }

    suspend fun getGlucoseResultByIdBetweenDates(id: String, startDate: Date, endDate: Date): List<GlucoseResult> {
        return researchResultDao.getGlucoseResultByIdBetweenDates(id, startDate, endDate)
    }

    private suspend fun calculateGbA1c(id: String) = runBlocking {
        val listOfGlucoseResult: MutableList<GlucoseResult>
        try {
            listOfGlucoseResult = researchResultDao.getResultsByUserId(id).toMutableList()

            var sum = 0.0
            for (glucoseResult in listOfGlucoseResult) {
                sum += if (glucoseResult.unit == "MG_PER_DL") {
                    glucoseResult.glucoseConcentration
                } else {
                    (glucoseResult.glucoseConcentration * 18.0182)
                }

            }

            val average = sum / listOfGlucoseResult.size
            return@runBlocking (average + 46.7) / 28.7
        }catch (e: Exception) {
            return@runBlocking 0.0
        }

    }
}

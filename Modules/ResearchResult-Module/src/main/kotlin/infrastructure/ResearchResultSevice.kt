package infrastructure


import form.GlucoseResult
import form.ResearchResultForm
import form.SafeDeleteResultForm
import form.UpdateResearchResultForm
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
}

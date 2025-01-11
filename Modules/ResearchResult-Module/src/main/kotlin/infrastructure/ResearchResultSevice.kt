package infrastructure


import form.ResearchResult
import form.ResearchResultForm
import form.SafeDeleteResultForm
import form.UpdateResearchResultForm
import java.util.UUID

class ResearchResultService(private val researchResultDao: ResearchResultDao) {

    suspend fun createResult(form: ResearchResultForm): UUID {
        validateForm(form)
        return researchResultDao.create(form)
    }

    suspend fun researchResult(id: String): ResearchResult {
        return researchResultDao.read(id)
    }

    suspend fun getAllResults(): List<ResearchResult> {
        return researchResultDao.getAll()
    }

    suspend fun getThreeResultsForId(id: String): List<ResearchResult> {
        return researchResultDao.getThreeResultsForUser(id)
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
        require(form.sequenceNumber > 0) { "Sequence number must be greater than 0" }
        require(form.glucoseConcentration > 0) { "Glucose concentration must be greater than 0" }
    }

    private fun validateUpdateForm(form: UpdateResearchResultForm) {
        require(form.sequenceNumber > 0) { "Sequence number must be greater than 0" }
        require(form.glucoseConcentration > 0) { "Glucose concentration must be greater than 0" }
    }
}

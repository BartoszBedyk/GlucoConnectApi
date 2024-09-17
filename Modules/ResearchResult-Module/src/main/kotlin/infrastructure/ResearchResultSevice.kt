package com.example.api.researchResults.service


import form.ResearchResult
import form.ResearchResultForm
import form.UpdateResearchResultForm
import infrastructure.ResearchResultDao
import java.util.UUID

class ResearchResultService(private val researchResultDao: ResearchResultDao) {

    suspend fun createResult(form: ResearchResultForm): UUID {
        validateForm(form)
        return researchResultDao.create(form)
    }

    suspend fun readResult(id: String): ResearchResult {
        return researchResultDao.read(id)
    }

    suspend fun getAllResults(): List<ResearchResult> {
        return researchResultDao.getAll()
    }

    suspend fun updateResult(form: UpdateResearchResultForm) {
        validateUpdateForm(form)
        researchResultDao.updateResult(form)
    }

    suspend fun deleteResult(id: String) {
        researchResultDao.deleteResult(id)
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

package infrastructure

import form.GlucoseResult
import form.ResearchResultForm
import form.SafeDeleteResultForm
import form.UpdateResearchResultForm
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.math.pow
import kotlin.math.sqrt

class ResearchResultService(private val researchResultDao: ResearchResultDao, private val secretKey: SecretKey) {

    suspend fun createGlucoseResult(form: ResearchResultForm): UUID {
        validateForm(form)
        return researchResultDao.createGlucoseResult(form, secretKey)
    }

    suspend fun syncGlucoseResults(result: GlucoseResult): GlucoseResult = researchResultDao.sync(result, secretKey)

    suspend fun getGlucoseResultById(id: String): GlucoseResult = researchResultDao.getGlucoseResultById(id, secretKey)

    suspend fun getAllResults(): List<GlucoseResult> = researchResultDao.getAll(secretKey)

    suspend fun getThreeResultsForId(id: String): List<GlucoseResult> = researchResultDao.getThreeResultsForUser(id, secretKey)

    suspend fun getResultsByUserId(id: String): List<GlucoseResult> = researchResultDao.getResultsByUserId(id, secretKey)

    suspend fun updateResult(form: UpdateResearchResultForm) {
        validateUpdateForm(form)
        researchResultDao.updateResult(form, secretKey)
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

    suspend fun getUserGbA1cById(id: String): Double = calculateGbA1c(id)

    suspend fun getGlucoseResultByIdBetweenDates(id: String, startDate: Date, endDate: Date): List<GlucoseResult> =
        researchResultDao.getGlucoseResultByIdBetweenDates(id, startDate, endDate, secretKey)

    suspend fun getDeviationById(id: String): Double = standardDeviation(id)

    private suspend fun calculateGbA1c(id: String) = runBlocking {
        val listOfGlucoseResult: MutableList<GlucoseResult>
        try {
            listOfGlucoseResult = researchResultDao.getResultsByUserId(id, secretKey).toMutableList()

            var sum = 0.0
            for (glucoseResult in listOfGlucoseResult) {
                sum += if (glucoseResult.unit == "MG_PER_DL") {
                    glucoseResult.glucoseConcentration
                } else {
                    (glucoseResult.glucoseConcentration * 18.0182)
                }
            }

            val average = sum / listOfGlucoseResult.size

            return@runBlocking BigDecimal((average + 46.7) / 28.7).setScale(2, RoundingMode.HALF_UP).toDouble()
        } catch (e: Exception) {
            return@runBlocking 0.0
        }
    }

    private suspend fun standardDeviation(id: String): Double = runBlocking {
        try {
            val results = researchResultDao.getResultsByUserId(id, secretKey)
                .take(93)
                .map { result ->
                    if (result.unit == "MG_PER_DL") {
                        result.glucoseConcentration
                    } else {
                        result.glucoseConcentration * 18.0182
                    }
                }

            if (results.isEmpty()) return@runBlocking 0.0

            val average = results.average()

            val variance = results.sumOf { (it - average).pow(2) } / results.size
            return@runBlocking BigDecimal(sqrt(variance))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        } catch (e: Exception) {
            return@runBlocking 0.0
        }
    }
}

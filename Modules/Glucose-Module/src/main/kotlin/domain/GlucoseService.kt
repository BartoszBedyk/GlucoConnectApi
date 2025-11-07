package domain

import data.GlucoseRepository
import java.util.UUID
import model.CreateGlucoseRequest
import model.GlucoseEntity

class GlucoseService(private val glucoseRepository: GlucoseRepository) {

    fun createGlucose(glucose: CreateGlucoseRequest): UUID = glucoseRepository.create(glucose)

    fun getGlucoseById(glucoseId: UUID): GlucoseEntity? = glucoseRepository.findById(glucoseId)
}

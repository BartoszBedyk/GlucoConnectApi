package domain

import data.GlucoseRepository
import model.CreateGlucoseRequest
import model.GlucoseEntity
import java.util.UUID

class GlucoseService(private val glucoseRepository: GlucoseRepository) {

    fun createGlucose(glucose: CreateGlucoseRequest): UUID = glucoseRepository.create(glucose)

    fun getGlucoseById(glucoseId: UUID): GlucoseEntity? = glucoseRepository.findById(glucoseId)
}

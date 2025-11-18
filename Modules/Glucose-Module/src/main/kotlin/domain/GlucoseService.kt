package domain

import data.GlucoseRepository
import model.CreateGlucoseRequest
import model.GlucoseEntity
import pageable.PageRequest
import pageable.PageResponse
import java.util.UUID

class GlucoseService(private val glucoseRepository: GlucoseRepository) {

    fun createGlucose(glucose: CreateGlucoseRequest): UUID = glucoseRepository.createGlucose(glucose)

    fun getGlucoseById(glucoseId: UUID): GlucoseEntity? = glucoseRepository.findGlucoseById(glucoseId)

    fun getAllGlucoses(pageRequest: PageRequest): PageResponse<GlucoseEntity> =
        glucoseRepository.findAllGlucose(pageRequest)
}

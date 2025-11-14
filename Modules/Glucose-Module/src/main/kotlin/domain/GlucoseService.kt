package domain

import data.GlucoseRepository
import model.CreateGlucoseRequest
import model.GlucoseEntity
import java.util.UUID
import pageable.PageRequest
import pageable.PageResponse

class GlucoseService(private val glucoseRepository: GlucoseRepository) {

    fun createGlucose(glucose: CreateGlucoseRequest): UUID = glucoseRepository.create(glucose)

    fun getGlucoseById(glucoseId: UUID): GlucoseEntity? = glucoseRepository.findById(glucoseId)

    fun getAllGlucoses(pageRequest: PageRequest): PageResponse<GlucoseEntity> = glucoseRepository.findAll(pageRequest)
}

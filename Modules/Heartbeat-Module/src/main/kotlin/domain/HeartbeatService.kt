package domain

import data.HeartbeatRepository
import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import pageable.PageRequest
import pageable.PageResponse
import java.util.UUID

class HeartbeatService(private val heartbeatRepository: HeartbeatRepository) {
    fun createHeartbeat(request: CreateHeartbeatRequest): UUID = heartbeatRepository.createHeartbeat(request)
    fun getHeartbeatById(uuid: UUID): HeartbeatEntity? = heartbeatRepository.findHeartbeatById(uuid)
    fun getHeartbeatsByUserId(req: PageRequest, uuid: UUID): PageResponse<HeartbeatEntity> =
        heartbeatRepository.findHeartbeatsByUserId(req, uuid)
}

package domain

import data.HeartbeatRepository
import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import pageable.PageRequest
import pageable.PageResponse
import java.util.UUID

class HeartbeatService(private val heartbeatRepository: HeartbeatRepository) {
    fun createHeartbeat(request: CreateHeartbeatRequest, userId: UUID): UUID =
        heartbeatRepository.createHeartbeat(request, userId)
    fun getHeartbeatById(uuid: UUID, userId: UUID): HeartbeatEntity? =
        heartbeatRepository.findHeartbeatById(uuid, userId)
    fun getHeartbeatsByUserId(req: PageRequest, uuid: UUID): PageResponse<HeartbeatEntity> =
        heartbeatRepository.findHeartbeatsByUserId(req, uuid)
}

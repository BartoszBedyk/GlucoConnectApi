package data

import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import pageable.PageRequest
import pageable.PageResponse
import pageable.paginate
import java.util.UUID

class HeartbeatRepository {

    private val sortMapping = mapOf(
        "id" to HeartbeatTable.id,
        "timestamp" to HeartbeatTable.timestamp,
        "createdAt" to HeartbeatTable.createdAt,
    )

    fun findHeartbeatById(id: UUID): HeartbeatEntity? = transaction {
        HeartbeatTable.select {
            HeartbeatTable.id eq id and (HeartbeatTable.deleted eq false)
        }
            .map { it.toHeartbeatEntity() }
            .singleOrNull()
    }

    fun findHeartbeatsByUserId(req: PageRequest, id: UUID): PageResponse<HeartbeatEntity> = transaction {
        paginate(
            table = HeartbeatTable,
            req = req,
            sortMapping = sortMapping
        ) {
            it.toHeartbeatEntity()
        }
    }

    fun createHeartbeat(request: CreateHeartbeatRequest): UUID = transaction {
        HeartbeatTable.insertAndGetId {
            it.fromCreateRequest(request, UUID.fromString("276aa762-9b5f-4d01-96d1-ab1d510d9c36"))
        }.value
    }
}

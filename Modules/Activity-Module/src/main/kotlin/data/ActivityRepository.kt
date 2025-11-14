package data

import model.ActivityEntity
import model.CreateActivityRequest
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import pageable.PageRequest
import pageable.PageResponse
import pageable.paginate

class ActivityRepository {
    private val sortMapping = mapOf(
        "id" to ActivityTable.id,
        "userId" to ActivityTable.userId,
        "value" to ActivityTable.value,
        "createdAt" to ActivityTable.createdAt
    )

    fun findAll(req: PageRequest): PageResponse<ActivityEntity> = transaction {
        paginate(
            table = ActivityTable,
            req = req,
            sortMapping = sortMapping

        ) { it.toActivityEntity() }

    }

    fun findById(id: Int): ActivityEntity? = transaction {
        ActivityTable.select { ActivityTable.id eq id }
            .map {
                it.toActivityEntity()
            }
            .singleOrNull()
    }

    fun findByUserId(userId: Int, req: PageRequest): PageResponse<ActivityEntity> = paginate(
        table = ActivityTable,
        baseQuery = { ActivityTable.userId eq userId },
        req = req,
        sortMapping = sortMapping
    ) { it.toActivityEntity() }

    fun create(request: CreateActivityRequest): Int = transaction {
        ActivityTable.insertAndGetId { it.fromCreateRequest(request) }.value
    }
}

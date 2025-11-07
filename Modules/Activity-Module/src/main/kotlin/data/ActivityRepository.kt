package data

import model.ActivityEntity
import model.CreateActivityRequest
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ActivityRepository {

    fun getAll(): List<ActivityEntity> = transaction {
        ActivityTable.selectAll().map {
            it.toActivityEntity()
        }
    }

    fun findById(id: Int): ActivityEntity? = transaction {
        ActivityTable.select { ActivityTable.id eq id }
            .map {
                it.toActivityEntity()
            }
            .singleOrNull()
    }

    fun findByUserId(userId: Int): List<ActivityEntity> = transaction {
        ActivityTable.select {
            ActivityTable.userId eq userId
        }
            .map {
                it.toActivityEntity()
            }
    }

    fun create(request: CreateActivityRequest): Int = transaction {
        ActivityTable.insertAndGetId { it.fromCreateRequest(request) }.value
    }
}

package data

import model.ActivityEntity
import model.CreateActivityRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

fun ResultRow.toActivityEntity(): ActivityEntity = ActivityEntity(
    id = this[ActivityTable.id].value,
    value = this[ActivityTable.value],
    userId = this[ActivityTable.userId],
    createdAt = this[ActivityTable.createdAt]
)

fun InsertStatement<*>.fromCreateRequest(request: CreateActivityRequest) {
    this[ActivityTable.value] = request.value
    this[ActivityTable.userId] = request.userId
}

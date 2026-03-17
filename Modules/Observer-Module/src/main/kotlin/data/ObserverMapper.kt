package data

import java.time.Instant
import java.util.UUID
import model.CreateObserverRequest
import model.ObserverEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

fun ResultRow.toObserverEntity() = ObserverEntity(
    id = this[ObserverTable.id].value,
    observer = this[ObserverTable.observer].value,
    observed = this[ObserverTable.observed].value,
    isAccepted = this[ObserverTable.isAccepted]
)

fun InsertStatement<*>.fromCreateRequest(request: CreateObserverRequest, userId: UUID) {
    this[ObserverTable.observer] = userId
    this[ObserverTable.observed] = request.observedId
    this[ObserverTable.createdAt] = Instant.now()
    this[ObserverTable.updatedAt] = Instant.now()
    this[ObserverTable.deleted] = false
}

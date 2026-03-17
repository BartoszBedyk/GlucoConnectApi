package data

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import model.CreateObserverRequest
import model.ObserverEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ObserverRepository {

    fun createObservation(request: CreateObserverRequest, userId: UUID) = transaction {
        ObserverTable.insertAndGetId {
            it.fromCreateRequest(request, userId)
        }.value
    }

    fun acceptObservation(id: UUID, userId: UUID) = transaction {
        ObserverTable.update({
            (ObserverTable.id eq id) and
                (ObserverTable.observed eq userId) and
                (ObserverTable.deleted eq false)}) {
            it[isAccepted] = true
            it[updatedAt] = Instant.now()
        }
    }

    fun unacceptObservation(id: UUID, userId: UUID) = transaction {
        ObserverTable.update({
            (ObserverTable.id eq id) and
            (ObserverTable.observed eq userId) and
            (ObserverTable.deleted eq false)}) {
            it[isAccepted] = false
            it[updatedAt] = Instant.now()
        }
    }

    fun getObservationRequests(uuid: UUID, accepted: Boolean): List<ObserverEntity> = transaction {
        ObserverTable.select {
            (ObserverTable.observed eq uuid) and
                (ObserverTable.isAccepted eq accepted) and
                (ObserverTable.deleted eq false)
        }.map { it.toObserverEntity() }
    }
}

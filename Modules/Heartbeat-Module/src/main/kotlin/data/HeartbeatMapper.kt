package data

import data.HeartbeatTable.systolicPressure
import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.Instant
import java.util.UUID

fun InsertStatement<*>.fromCreateRequest(request: CreateHeartbeatRequest, userId: UUID) {
    this[HeartbeatTable.user] = userId
    this[HeartbeatTable.systolicPressure] = request.systolicPressure
    this[HeartbeatTable.diastolicPressure] = request.diastolicPressure
    this[HeartbeatTable.pulse] = request.pulse
    this[HeartbeatTable.timestamp] = request.timestamp
    this[HeartbeatTable.note] = request.note
    this[HeartbeatTable.createdAt] = request.createdAt ?: Instant.now()
    this[HeartbeatTable.updatedAt] = request.updatedAt ?: Instant.now()
    this[HeartbeatTable.deleted] = false
}

fun ResultRow.toHeartbeatEntity() = HeartbeatEntity(
    id = this[HeartbeatTable.id].value,
    systolicPressure = this[HeartbeatTable.systolicPressure],
    diastolicPressure = this[HeartbeatTable.diastolicPressure],
    pulse = this[HeartbeatTable.pulse],
    timestamp = this[HeartbeatTable.timestamp],
    note = this[HeartbeatTable.note],
    createdAt = this[HeartbeatTable.createdAt],
    updatedAt = this[HeartbeatTable.updatedAt]
)

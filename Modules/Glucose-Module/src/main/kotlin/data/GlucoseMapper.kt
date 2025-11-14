package data

import model.CreateGlucoseRequest
import model.GlucoseEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.Instant

fun ResultRow.toGlucoseEntity() = GlucoseEntity(
    id = this[GlucoseTable.id].value,
    concentration = this[GlucoseTable.concentration],
    unit = this[GlucoseTable.unit],
    timestamp = this[GlucoseTable.timestamp],
    afterMedication = this[GlucoseTable.afterMedication],
    afterMeal = this[GlucoseTable.afterMeal],
    note = this[GlucoseTable.note],
    createdAt = this[GlucoseTable.createdAt],
    updatedAt = this[GlucoseTable.updatedAt]
)

fun InsertStatement<*>.fromCreateRequest(request: CreateGlucoseRequest) {
    this[GlucoseTable.concentration] = request.concentration
    this[GlucoseTable.unit] = request.unit
    this[GlucoseTable.timestamp] = request.timestamp
    this[GlucoseTable.afterMedication] = request.afterMedication
    this[GlucoseTable.afterMeal] = request.afterMeal
    this[GlucoseTable.note] = request.note
    this[GlucoseTable.createdAt] = request.createdAt ?: Instant.now()
    this[GlucoseTable.updatedAt] = request.updatedAt ?: Instant.now()
}

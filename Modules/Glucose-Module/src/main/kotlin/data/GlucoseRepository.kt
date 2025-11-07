package data

import model.GlucoseEntity
import java.time.Instant
import java.util.UUID
import model.CreateGlucoseRequest
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GlucoseRepository {

    fun findById(id: UUID): GlucoseEntity? = transaction {
        GlucoseTable
            .select { GlucoseTable.id eq id and (GlucoseTable.deleted eq false) }
            .map { it.toGlucoseEntity() }
            .singleOrNull()
    }

    fun create(request: CreateGlucoseRequest): UUID = transaction {
        GlucoseTable.insertAndGetId { it.fromCreateRequest(request) }.value
    }

}

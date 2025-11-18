package data

import model.CreateGlucoseRequest
import model.GlucoseEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import pageable.PageRequest
import pageable.PageResponse
import pageable.paginate
import java.util.UUID

class GlucoseRepository {

    private val sortMapping = mapOf(
        "id" to GlucoseTable.id,
        "timestamp" to GlucoseTable.timestamp,
        "afterMedication" to GlucoseTable.afterMedication,
        "afterMeal" to GlucoseTable.afterMeal,
        "createdAt" to GlucoseTable.createdAt,
        "concentration" to GlucoseTable.concentration
    )

    fun findAllGlucose(req: PageRequest): PageResponse<GlucoseEntity> = transaction {
        paginate(
            table = GlucoseTable,
            req = req,
            sortMapping = sortMapping
        ) {
            it.toGlucoseEntity()
        }
    }

    fun findGlucoseById(id: UUID): GlucoseEntity? = transaction {
        GlucoseTable
            .select { GlucoseTable.id eq id and (GlucoseTable.deleted eq false) }
            .map { it.toGlucoseEntity() }
            .singleOrNull()
    }

    fun createGlucose(request: CreateGlucoseRequest): UUID = transaction {
        GlucoseTable.insertAndGetId { it.fromCreateRequest(request) }.value
    }
}

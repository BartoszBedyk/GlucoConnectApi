package data

import model.CreateDrugRequest
import model.DrugEntity
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DrugRepository {

    fun createDrug(createForm: CreateDrugRequest) = transaction {
        DrugTable.insertAndGetId {
            it.fromCreateRequest(createForm)
        }.value
    }

    fun findDrugById(id: Long): DrugEntity? = transaction {
        DrugTable.select {
            DrugTable.id eq id
        }.map { it.toDrugEntity() }.singleOrNull()
    }
}

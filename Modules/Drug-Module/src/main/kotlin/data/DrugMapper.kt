package data

import model.CreateDrugRequest
import model.DrugEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

fun InsertStatement<*>.fromCreateRequest(request: CreateDrugRequest) {
    this[DrugTable.name] = request.name
    this[DrugTable.description] = request.description
    this[DrugTable.manufacturer] = request.manufacturer
    this[DrugTable.form] = request.form
    this[DrugTable.strength] = request.strength
}

fun ResultRow.toDrugEntity() = DrugEntity(
    id = this[DrugTable.id].value,
    name = this[DrugTable.name],
    description = this[DrugTable.description],
    manufacturer = this[DrugTable.manufacturer],
    form = this[DrugTable.form],
    strength = this[DrugTable.strength]

)

package data

import model.CreateUserRequest
import InnerUserEntity
import model.UserEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.Instant

fun InsertStatement<*>.fromCreateRequest(request: CreateUserRequest) {
    this[UserTable.firstName] = request.firstName
    this[UserTable.lastName] = request.lastName
    this[UserTable.email] = request.email
    this[UserTable.type] = request.type
    this[UserTable.prefUnit] = request.prefUnit
    this[UserTable.createdAt] = Instant.now()
    this[UserTable.updatedAt] = Instant.now()
    this[UserTable.deleted] = false
}

fun ResultRow.toUserEntity() = UserEntity(
    firstName = this[UserTable.firstName],
    lastName = this[UserTable.lastName],
    email = this[UserTable.email],
    type = this[UserTable.type],
    prefUnit = this[UserTable.prefUnit],
    createdAt = this[UserTable.createdAt],
    updatedAt = this[UserTable.updatedAt]
)

fun ResultRow.toInnerUserEntity() = InnerUserEntity(
    id = this[UserTable.id].toString(),
    email = this[UserTable.email],
    type = this[UserTable.type].toString()
)

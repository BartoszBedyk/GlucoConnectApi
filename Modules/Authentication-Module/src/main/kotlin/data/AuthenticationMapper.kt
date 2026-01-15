package data

import InnerUserEntity
import model.AuthenticationCredentials
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement

fun InsertStatement<*>.fromCreateRequest(request: AuthenticationCredentials) {
    this[AuthenticationTable.passwordHash] = request.password
    this[AuthenticationTable.loginName] = request.username
}

fun ResultRow.toInnerUserEntity() = InnerUserEntity(
    id = this[AuthenticationTable.id].toString(),
    email = this[AuthenticationTable.loginName],
    type = ""
)

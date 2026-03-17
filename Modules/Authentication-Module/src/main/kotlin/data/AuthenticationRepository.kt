package data

import InnerUserEntity
import model.AuthenticationCredentials
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import verifyPassword

class AuthenticationRepository {

    fun login(authData: AuthenticationCredentials): InnerUserEntity? = transaction {
        // addLogger(StdOutSqlLogger)
        val row = (AuthenticationTable)
            .select {
                (AuthenticationTable.loginName eq authData.username) and
                    (AuthenticationTable.failedAttempts lessEq 3) and
                    (AuthenticationTable.blocked eq false) and
                    (AuthenticationTable.deleted eq false)
            }
            .singleOrNull()

        if (row != null && verifyPassword(authData.password, row[AuthenticationTable.passwordHash])) {
            row.toInnerUserEntity()
        } else {
            null
        }
    }

    fun create(authData: AuthenticationCredentials): InnerUserEntity = transaction {
        val id = AuthenticationTable.insertAndGetId {
            it.fromCreateRequest(authData)
        }

        AuthenticationTable
            .select { AuthenticationTable.id eq id.value }
            .single()
            .toInnerUserEntity()
    }
}

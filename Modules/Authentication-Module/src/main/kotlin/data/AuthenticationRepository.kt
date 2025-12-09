package data

import hashPassword
import model.AuthenticationCredentials
import model.InnerUserEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AuthenticationRepository {

    fun login(authData: AuthenticationCredentials): InnerUserEntity? = transaction {
        (UserTable innerJoin AuthenticationTable)
            .select {
                (
                    (AuthenticationTable.loginName eq authData.username) or
                        (UserTable.email eq authData.username)

                    ) and
                    (AuthenticationTable.passwordHash eq hashPassword(authData.password)) and
                    (AuthenticationTable.failedAttempts lessEq 3) and
                    (AuthenticationTable.blocked eq false) and
                    (AuthenticationTable.deleted eq false)
            }
            .map { it.toInnerUserEntity() }.singleOrNull()
    }
}

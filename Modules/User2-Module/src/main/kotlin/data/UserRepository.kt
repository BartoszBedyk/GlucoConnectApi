package data

import model.CreateUserRequest
import model.UserEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository {

    fun createUser(request: CreateUserRequest): UUID = transaction {
        UserTable.insertAndGetId { it.fromCreateRequest(request) }.value
    }

    fun findUserById(id: UUID): UserEntity? = transaction {
        UserTable.select { UserTable.id eq id and (UserTable.deleted eq false) }
            .map { it.toUserEntity() }
            .singleOrNull()
    }
}

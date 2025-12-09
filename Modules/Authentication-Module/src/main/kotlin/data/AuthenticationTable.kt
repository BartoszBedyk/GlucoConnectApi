package data

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object AuthenticationTable : UUIDTable("authentication") {
    val userId = reference("user_id", UserTable).nullable()
    val passwordHash = varchar("password_hash", 255)
    val loginName = varchar("login_name", 50).uniqueIndex()
    val lastLoginAt = timestamp("last_login_at").nullable()
    val failedAttempts = integer("failed_attempts").default(0)
    val blocked = bool("locked").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val deleted = bool("deleted").default(false)
}

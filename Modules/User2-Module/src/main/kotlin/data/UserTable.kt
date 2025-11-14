package data

import model.GlucoseUnit
import model.UserType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp


object UserTable : UUIDTable("user"){
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 50)
    val type = enumerationByName("type", 20, UserType::class)
    val prefUnit = enumerationByName("prefUnit", 20, GlucoseUnit::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val deleted = bool("deleted").default(false)
}

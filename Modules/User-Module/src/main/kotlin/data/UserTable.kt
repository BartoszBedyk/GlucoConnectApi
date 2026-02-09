package data

import model.GlucoseUnit
import model.UserType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserTable : UUIDTable("user_gc") {
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 50)
    val type = enumerationByName("type", 20, UserType::class)
    val prefUnit = enumerationByName("pref_unit", 20, GlucoseUnit::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val deleted = bool("deleted").default(false)

    val authentication = reference(
        name = "authentication_id",
        foreign = AuthenticationTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.NO_ACTION,
        fkName = "fk_user_authentication_id",
    )
}

package data

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object GlucoseTable : UUIDTable("glucose") {
    val concentration = double("concentration")
    val unit = enumerationByName("unit", 20, GlucoseUnit::class)
    val timestamp = timestamp("timestamp")
    val afterMedication = bool("after_medication").default(false)
    val afterMeal = bool("after_meal").default(false)
    val note = text("note").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val deleted = bool("deleted").default(false)

    val user = reference(
        name = "user_id",
        foreign = UserTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.NO_ACTION,
        fkName = "fk_glucose_user_id",)

}

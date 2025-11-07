package data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object ActivityTable : IntIdTable("activities") {
    val value = text("value")
    val userId = integer("user_id")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

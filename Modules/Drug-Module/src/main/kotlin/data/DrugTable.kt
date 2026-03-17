package data

import org.jetbrains.exposed.dao.id.LongIdTable

object DrugTable : LongIdTable("drug") {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val manufacturer = varchar("manufacturer", 255)
    val form = varchar("form", 255)
    val strength = varchar("strength", 255)
}

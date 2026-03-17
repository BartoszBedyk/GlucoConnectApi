package data

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object ObserverTable: UUIDTable("observer")  {
    val observer = reference(
        name = "user_id",
        foreign = AuthenticationTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.NO_ACTION,
        fkName = "fk_observer_authentication_id",
    )

    val observed = reference(
        name = "observed_id",
        foreign = AuthenticationTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.NO_ACTION,
        fkName = "fk_observed_authentication_id",
    )

    val isAccepted = bool("is_accepted").default(false)

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
    val deleted = bool("deleted").default(false)

    init{
        uniqueIndex("uq_observer_observed", observer, observed)
        check("chk_not_self_observe") { observer neq observed }
    }
}

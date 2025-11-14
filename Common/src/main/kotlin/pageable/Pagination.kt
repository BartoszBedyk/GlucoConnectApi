package pageable

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> paginate(
    table: Table,
    baseQuery: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
    req: PageRequest,
    sortMapping: Map<String, Expression<*>>,
    rowMapper: (ResultRow) -> T
): PageResponse<T> = transaction {
    val selectQuery = if (baseQuery == null) {
        table.selectAll()
    } else {
        table.select(baseQuery)
    }

    val total = selectQuery.count()

    val offset = (req.page - 1) * req.size
    val pagedQuery = selectQuery
        .limit(req.size, offset.toLong())

    req.sortField?.let { field ->
        val column = sortMapping[field]
        if (column != null) {
            pagedQuery.orderBy(
                column,
                if (req.sortDirection == SortDirection.ASC) SortOrder.ASC else SortOrder.DESC
            )
        }
    }

    val content = pagedQuery.map(rowMapper)

    PageResponse(
        content = content,
        page = req.page,
        size = req.size,
        total = total
    )
}

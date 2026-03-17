package pageable

import io.ktor.server.application.ApplicationCall

fun ApplicationCall.pageRequest(): PageRequest {
    val page = request.queryParameters["page"]?.toIntOrNull() ?: 1
    val size = request.queryParameters["size"]?.toIntOrNull() ?: 20

    val rawSort = request.queryParameters["sort"]
    val sortField = rawSort?.substringBefore(",")?.trim()
    val sortDirection = rawSort?.substringAfter(",", "")?.trim()?.let {
        if (it.equals("desc", true)) SortDirection.DESC else SortDirection.ASC
    } ?: SortDirection.ASC

    return PageRequest(page, size, sortField, sortDirection)
}

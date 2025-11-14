package pageable

import kotlinx.serialization.Serializable

@Serializable
data class PageRequest(
    val page: Int = 1,
    val size: Int = 10,
    val sortField: String? = null,
    val sortDirection: SortDirection = SortDirection.ASC
)

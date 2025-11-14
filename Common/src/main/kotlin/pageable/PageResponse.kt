package pageable

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(val content: List<T>, val page: Int, val size: Int, val total: Long)

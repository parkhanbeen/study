package com.anoncommunity.contract.model

data class DomainPageable(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: SortDirection = SortDirection.DESC,
) {
    init {
        require(page >= 0) { "Page must be non-negative" }
        require(page <= MAX_PAGE) { "Page must not exceed $MAX_PAGE" }
        require(size in 1..100) { "Page size must be between 1 and 100" }
        require(SORT_FIELD_PATTERN.matches(sortBy)) { "Invalid sortBy field: $sortBy" }
    }

    companion object {
        const val MAX_PAGE = 100
        private val SORT_FIELD_PATTERN = Regex("[a-zA-Z][a-zA-Z0-9_]*")
    }
}

enum class SortDirection { ASC, DESC }

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

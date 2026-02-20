package com.anoncommunity.app.usecase.search

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.Tag
import com.anoncommunity.domain.post.port.CursorPageResult
import com.anoncommunity.domain.post.port.CursorSearchQuery
import com.anoncommunity.domain.post.port.PostSearchHit
import com.anoncommunity.domain.post.port.PostSearchPort
import com.anoncommunity.domain.post.port.PostSearchQuery
import org.springframework.stereotype.Service

@Service
class SearchPostUseCase(
    private val postSearchPort: PostSearchPort,
) : QueryUseCase<SearchPostQuery, PageResult<Post>> {

    override fun invoke(query: SearchPostQuery): PageResult<Post> {
        return postSearchPort.search(
            PostSearchQuery(
                keyword = query.keyword,
                tags = query.tags?.map { Tag(it) },
                category = query.category,
                language = query.language,
                pageable = query.pageable,
            )
        )
    }

    fun searchWithCursor(query: CursorSearchPostQuery): CursorPageResult<Post> {
        return postSearchPort.searchWithCursor(
            CursorSearchQuery(
                keyword = query.keyword,
                tags = query.tags?.map { Tag(it) },
                category = query.category,
                language = query.language,
                size = query.size,
                searchAfter = query.searchAfter,
                sortBy = query.sortBy,
                dateRange = query.dateRange,
            )
        )
    }

    fun searchHitsWithCursor(query: CursorSearchPostQuery): CursorPageResult<PostSearchHit> {
        return postSearchPort.searchHitsWithCursor(
            CursorSearchQuery(
                keyword = query.keyword,
                tags = query.tags?.map { Tag(it) },
                category = query.category,
                language = query.language,
                size = query.size,
                searchAfter = query.searchAfter,
                sortBy = query.sortBy,
                dateRange = query.dateRange,
            )
        )
    }
}

data class SearchPostQuery(
    val keyword: String? = null,
    val tags: List<String>? = null,
    val category: String? = null,
    val language: String? = null,
    val pageable: DomainPageable,
)

data class CursorSearchPostQuery(
    val keyword: String? = null,
    val tags: List<String>? = null,
    val category: String? = null,
    val language: String? = null,
    val size: Int = 20,
    val searchAfter: List<Any>? = null,
    val sortBy: String = "createdAt",
    val dateRange: String? = null,
)

package com.anoncommunity.domain.post.port

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.Tag

data class PostSearchHit(
    val post: Post,
    val highlights: Map<String, List<String>> = emptyMap(),
)

interface PostSearchPort {
    fun search(query: PostSearchQuery): PageResult<Post>
    fun searchWithCursor(query: CursorSearchQuery): CursorPageResult<Post>
    fun searchHitsWithCursor(query: CursorSearchQuery): CursorPageResult<PostSearchHit>
    fun getHotTags(limit: Int = 10): List<String>
}

interface PostFeedPort {
    fun getFeed(category: String?, pageable: DomainPageable): PageResult<Post>
    fun getFollowingFeed(followingProfileIds: List<Long>, pageable: DomainPageable): PageResult<Post>
}

data class PostSearchQuery(
    val keyword: String? = null,
    val tags: List<Tag>? = null,
    val category: String? = null,
    val language: String? = null,
    val pageable: DomainPageable,
)

data class CursorSearchQuery(
    val keyword: String? = null,
    val tags: List<Tag>? = null,
    val category: String? = null,
    val language: String? = null,
    val size: Int = 20,
    val searchAfter: List<Any>? = null,
    val sortBy: String = "createdAt",
    val dateRange: String? = null,
)

data class CursorPageResult<T>(
    val content: List<T>,
    val size: Int,
    val searchAfter: List<Any>?,
    val hasNext: Boolean,
)

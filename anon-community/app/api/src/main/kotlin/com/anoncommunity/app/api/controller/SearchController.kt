package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.config.LanguageContext
import com.anoncommunity.app.api.dto.PageResponse
import com.anoncommunity.app.api.dto.PostResponse
import com.anoncommunity.app.api.dto.PostSearchHitResponse
import com.anoncommunity.app.usecase.search.CursorSearchPostQuery
import com.anoncommunity.app.usecase.search.GetHotTagsUseCase
import com.anoncommunity.app.usecase.search.SearchPostQuery
import com.anoncommunity.app.usecase.search.SearchPostUseCase
import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.domain.post.port.CursorPageResult
import com.anoncommunity.infra.redis.config.RedisCacheConfig.Companion.CACHE_HOT_TAGS
import com.anoncommunity.infra.redis.config.RedisCacheConfig.Companion.CACHE_SEARCH
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Search", description = "검색 API")
@RestController
@RequestMapping("/api/v1/search")
@Validated
class SearchController(
    private val searchPostUseCase: SearchPostUseCase,
    private val getHotTagsUseCase: GetHotTagsUseCase,
) {

    companion object {
        private val ALLOWED_SEARCH_SORT_FIELDS = setOf("createdAt", "likeCount", "commentCount", "viewCount", "relevance")
        private val ALLOWED_DATE_RANGES = setOf("today", "this_week", "this_month")
    }

    @Operation(summary = "게시글 검색 (offset 기반)")
    @GetMapping("/posts")
    @Cacheable(
        cacheNames = [CACHE_SEARCH],
        key = "'offset:' + #keyword + ':' + #tags + ':' + #category + ':' + #language + ':' + #page + ':' + #size",
    )
    fun searchPosts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): ResponseEntity<PageResponse<PostResponse>> {
        val resolvedLanguage = language ?: LanguageContext.getLanguage()

        val result = searchPostUseCase(
            SearchPostQuery(
                keyword = keyword,
                tags = tags,
                category = category,
                language = resolvedLanguage,
                pageable = DomainPageable(page = page, size = size),
            )
        )
        return ResponseEntity.ok(PageResponse.from(result) { PostResponse.from(it) })
    }

    @Operation(summary = "게시글 검색 (cursor 기반, 대량 페이지네이션, 하이라이팅 포함)")
    @GetMapping("/posts/cursor")
    @Cacheable(
        cacheNames = [CACHE_SEARCH],
        key = "'cursor:' + #keyword + ':' + #tags + ':' + #category + ':' + #language + ':' + #size + ':' + #searchAfter + ':' + #sortBy + ':' + #dateRange",
        condition = "#searchAfter == null",
    )
    fun searchPostsWithCursor(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(required = false) searchAfter: String?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(required = false) dateRange: String?,
    ): ResponseEntity<CursorSearchResponse<PostSearchHitResponse>> {
        val resolvedLanguage = language ?: LanguageContext.getLanguage()
        val sortValues = searchAfter?.split(",")?.map { it as Any }
        val validSortBy = if (sortBy in ALLOWED_SEARCH_SORT_FIELDS) sortBy else "createdAt"
        val validDateRange = if (dateRange in ALLOWED_DATE_RANGES) dateRange else null

        val result = searchPostUseCase.searchHitsWithCursor(
            CursorSearchPostQuery(
                keyword = keyword,
                tags = tags,
                category = category,
                language = resolvedLanguage,
                size = size,
                searchAfter = sortValues,
                sortBy = validSortBy,
                dateRange = validDateRange,
            )
        )

        return ResponseEntity.ok(
            CursorSearchResponse(
                content = result.content.map { PostSearchHitResponse.from(it) },
                size = result.size,
                searchAfter = result.searchAfter?.joinToString(","),
                hasNext = result.hasNext,
            )
        )
    }

    @Operation(summary = "인기 태그 조회")
    @GetMapping("/hot-tags")
    @Cacheable(
        cacheNames = [CACHE_HOT_TAGS],
        key = "'hot-tags:' + #limit",
    )
    fun getHotTags(
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) limit: Int,
    ): ResponseEntity<HotTagsResponse> {
        val tags = getHotTagsUseCase(limit)
        return ResponseEntity.ok(HotTagsResponse(tags))
    }
}

data class CursorSearchResponse<T>(
    val content: List<T>,
    val size: Int,
    val searchAfter: String?,
    val hasNext: Boolean,
)

data class HotTagsResponse(val tags: List<String>)

package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.dto.PageResponse
import com.anoncommunity.app.api.dto.PostDetailResponse
import com.anoncommunity.app.usecase.feed.GetFeedQuery
import com.anoncommunity.app.usecase.feed.GetFeedUseCase
import com.anoncommunity.app.usecase.feed.GetFollowingFeedQuery
import com.anoncommunity.app.usecase.feed.GetFollowingFeedUseCase
import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.SortDirection
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Feed", description = "피드 API")
@RestController
@RequestMapping("/api/v1/feed")
@Validated
class FeedController(
    private val getFeedUseCase: GetFeedUseCase,
    private val getFollowingFeedUseCase: GetFollowingFeedUseCase,
) {
    @Operation(summary = "전체 피드")
    @GetMapping
    fun getFeed(
        @RequestParam(required = false) category: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
    ): ResponseEntity<PageResponse<PostDetailResponse>> {
        val direction = runCatching { SortDirection.valueOf(sortDirection.uppercase()) }
            .getOrDefault(SortDirection.DESC)
        val validSortBy = if (sortBy in ALLOWED_SORT_FIELDS) sortBy else "createdAt"

        val result = getFeedUseCase(
            GetFeedQuery(
                category = category,
                pageable = DomainPageable(
                    page = page,
                    size = size,
                    sortBy = validSortBy,
                    sortDirection = direction,
                ),
            )
        )
        return ResponseEntity.ok(PageResponse.from(result) { PostDetailResponse.from(it) })
    }

    @Operation(summary = "팔로잉 피드")
    @GetMapping("/following")
    fun getFollowingFeed(
        @RequestHeader("X-Profile-Id") activeProfileId: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
    ): ResponseEntity<PageResponse<PostDetailResponse>> {
        val direction = runCatching { SortDirection.valueOf(sortDirection.uppercase()) }
            .getOrDefault(SortDirection.DESC)
        val validSortBy = if (sortBy in ALLOWED_SORT_FIELDS) sortBy else "createdAt"

        val result = getFollowingFeedUseCase(
            GetFollowingFeedQuery(
                activeProfileId = activeProfileId,
                pageable = DomainPageable(
                    page = page,
                    size = size,
                    sortBy = validSortBy,
                    sortDirection = direction,
                ),
            )
        )
        return ResponseEntity.ok(PageResponse.from(result) { PostDetailResponse.from(it) })
    }

    companion object {
        private val ALLOWED_SORT_FIELDS = setOf("createdAt", "updatedAt", "commentCount", "likeCount", "viewCount")
    }
}

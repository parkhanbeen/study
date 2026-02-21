package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.dto.PageResponse
import com.anoncommunity.app.api.dto.ProfileResponse
import com.anoncommunity.app.usecase.follow.GetFollowersQuery
import com.anoncommunity.app.usecase.follow.GetFollowersUseCase
import com.anoncommunity.app.usecase.follow.GetFollowingQuery
import com.anoncommunity.app.usecase.follow.GetFollowingUseCase
import com.anoncommunity.app.usecase.member.GetPublicProfileQuery
import com.anoncommunity.app.usecase.member.GetPublicProfileUseCase
import com.anoncommunity.contract.model.DomainPageable
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Profile", description = "프로필 API")
@RestController
@RequestMapping("/api/v1/profiles")
@Validated
class ProfileController(
    private val getPublicProfileUseCase: GetPublicProfileUseCase,
    private val getFollowersUseCase: GetFollowersUseCase,
    private val getFollowingUseCase: GetFollowingUseCase,
) {
    @Operation(summary = "공개 프로필 조회")
    @GetMapping("/{profileId}")
    fun getProfile(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id", required = false) requesterProfileId: Long?,
    ): ResponseEntity<ProfileResponse> {
        val result = getPublicProfileUseCase(GetPublicProfileQuery(profileId, requesterProfileId))
        return ResponseEntity.ok(
            ProfileResponse.from(
                result.profile,
                isFollowing = result.isFollowing,
                isMe = requesterProfileId == profileId,
            )
        )
    }

    @Operation(summary = "팔로워 목록")
    @GetMapping("/{profileId}/followers")
    fun getFollowers(
        @PathVariable profileId: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): ResponseEntity<PageResponse<ProfileResponse>> {
        val result = getFollowersUseCase(GetFollowersQuery(profileId, DomainPageable(page, size)))
        return ResponseEntity.ok(PageResponse.from(result) { ProfileResponse.from(it) })
    }

    @Operation(summary = "팔로잉 목록")
    @GetMapping("/{profileId}/following")
    fun getFollowing(
        @PathVariable profileId: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): ResponseEntity<PageResponse<ProfileResponse>> {
        val result = getFollowingUseCase(GetFollowingQuery(profileId, DomainPageable(page, size)))
        return ResponseEntity.ok(PageResponse.from(result) { ProfileResponse.from(it) })
    }
}

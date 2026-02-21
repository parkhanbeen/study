package com.anoncommunity.app.api.controller

import com.anoncommunity.app.usecase.follow.FollowCommand
import com.anoncommunity.app.usecase.follow.FollowUseCase
import com.anoncommunity.app.usecase.follow.UnfollowCommand
import com.anoncommunity.app.usecase.follow.UnfollowUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Follow", description = "팔로우 API")
@RestController
@RequestMapping("/api/v1/profiles/{profileId}")
class FollowController(
    private val followUseCase: FollowUseCase,
    private val unfollowUseCase: UnfollowUseCase,
) {
    @Operation(summary = "팔로우")
    @PostMapping("/follow")
    fun follow(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id") followerProfileId: Long,
    ): ResponseEntity<Unit> {
        followUseCase(FollowCommand(followerProfileId = followerProfileId, followingProfileId = profileId))
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "언팔로우")
    @DeleteMapping("/follow")
    fun unfollow(
        @PathVariable profileId: Long,
        @RequestHeader("X-Profile-Id") followerProfileId: Long,
    ): ResponseEntity<Unit> {
        unfollowUseCase(UnfollowCommand(followerProfileId = followerProfileId, followingProfileId = profileId))
        return ResponseEntity.noContent().build()
    }
}

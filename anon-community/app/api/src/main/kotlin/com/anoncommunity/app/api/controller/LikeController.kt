package com.anoncommunity.app.api.controller

import com.anoncommunity.app.usecase.like.*
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
class LikeController(
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
) {
    @PostMapping
    fun likePost(
        @PathVariable postId: Long,
        @RequestHeader("X-Profile-Id") profileId: Long,
    ): ResponseEntity<Unit> {
        likePostUseCase(LikePostCommand(postId = postId, memberId = profileId))
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping
    fun unlikePost(
        @PathVariable postId: Long,
        @RequestHeader("X-Profile-Id") profileId: Long,
    ): ResponseEntity<Unit> {
        unlikePostUseCase(UnlikePostCommand(postId = postId, memberId = profileId))
        return ResponseEntity.noContent().build()
    }
}

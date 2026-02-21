package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.dto.*
import com.anoncommunity.app.usecase.comment.*
import com.anoncommunity.contract.model.DomainPageable
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@Validated
class CommentController(
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
) {
    @PostMapping
    fun createComment(
        @PathVariable postId: Long,
        @RequestHeader("X-Member-Id") memberId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
    ): ResponseEntity<CommentResponse> {
        val comment = createCommentUseCase(
            CreateCommentCommand(
                postId = postId,
                authorId = memberId,
                content = request.content,
                parentCommentId = request.parentCommentId,
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment))
    }

    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @RequestHeader("X-Member-Id") memberId: Long,
        @Valid @RequestBody request: UpdateCommentRequest,
    ): ResponseEntity<CommentResponse> {
        val comment = updateCommentUseCase(
            UpdateCommentCommand(
                commentId = commentId,
                requesterId = memberId,
                content = request.content,
            )
        )
        return ResponseEntity.ok(CommentResponse.from(comment))
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @RequestHeader("X-Member-Id") memberId: Long,
    ): ResponseEntity<Unit> {
        deleteCommentUseCase(DeleteCommentCommand(commentId = commentId, requesterId = memberId))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getComments(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): ResponseEntity<PageResponse<CommentResponse>> {
        val result = getCommentsUseCase(
            GetCommentsQuery(
                postId = postId,
                pageable = DomainPageable(page = page, size = size),
            )
        )
        return ResponseEntity.ok(PageResponse.from(result) { CommentResponse.from(it) })
    }
}

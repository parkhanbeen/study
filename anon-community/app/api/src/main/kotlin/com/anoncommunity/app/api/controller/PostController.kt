package com.anoncommunity.app.api.controller

import com.anoncommunity.infra.redis.config.RedisCacheConfig.Companion.CACHE_SEARCH
import com.anoncommunity.app.api.dto.*
import com.anoncommunity.app.usecase.post.*
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Post", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts")
class PostController(
    private val createPostUseCase: CreatePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val getPostUseCase: GetPostUseCase,
) {
    @PostMapping
    @CacheEvict(cacheNames = [CACHE_SEARCH], allEntries = true)
    fun createPost(
        @RequestHeader("X-Profile-Id") profileId: Long,
        @Valid @RequestBody request: CreatePostRequest,
    ): ResponseEntity<PostResponse> {
        val post = createPostUseCase(
            CreatePostCommand(
                authorProfileId = profileId,
                title = request.title,
                content = request.content,
                category = request.category,
                tags = request.tags,
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post))
    }

    @PutMapping("/{postId}")
    @CacheEvict(cacheNames = [CACHE_SEARCH], allEntries = true)
    fun updatePost(
        @PathVariable postId: Long,
        @RequestHeader("X-Profile-Id") profileId: Long,
        @Valid @RequestBody request: UpdatePostRequest,
    ): ResponseEntity<PostResponse> {
        val post = updatePostUseCase(
            UpdatePostCommand(
                postId = postId,
                requesterId = profileId,
                title = request.title,
                content = request.content,
                category = request.category,
                tags = request.tags,
            )
        )
        return ResponseEntity.ok(PostResponse.from(post))
    }

    @DeleteMapping("/{postId}")
    @CacheEvict(cacheNames = [CACHE_SEARCH], allEntries = true)
    fun deletePost(
        @PathVariable postId: Long,
        @RequestHeader("X-Profile-Id") profileId: Long,
    ): ResponseEntity<Unit> {
        deletePostUseCase(DeletePostCommand(postId = postId, requesterProfileId = profileId))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostDetailResponse> {
        val result = getPostUseCase(GetPostQuery(postId))
        return ResponseEntity.ok(PostDetailResponse.from(result))
    }
}

package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.exception.ApiExceptionHandler
import com.anoncommunity.app.usecase.post.*
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.*
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class PostControllerSpec : DescribeSpec({

    val createPostUseCase = mockk<CreatePostUseCase>()
    val updatePostUseCase = mockk<UpdatePostUseCase>()
    val deletePostUseCase = mockk<DeletePostUseCase>()
    val getPostUseCase = mockk<GetPostUseCase>()

    val controller = PostController(createPostUseCase, updatePostUseCase, deletePostUseCase, getPostUseCase)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(ApiExceptionHandler())
        .setValidator(LocalValidatorFactoryBean().apply { afterPropertiesSet() })
        .build()
    val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    beforeEach {
        clearMocks(createPostUseCase, updatePostUseCase, deletePostUseCase, getPostUseCase)
    }

    fun createTestPost(
        id: Long = 1L,
        authorId: Long = 100L,
        title: String = "Test Title",
        content: String = "Test Content",
        tags: List<String> = listOf("kotlin"),
    ): Post = Post.create(
        id = PostId(id),
        authorId = MemberId(authorId),
        title = PostTitle(title),
        content = PostContent(content),
        tags = tags.map { Tag(it) },
    )

    describe("POST /api/v1/posts") {

        it("should return 201 with PostResponse on valid request") {
            val testPost = createTestPost()
            every { createPostUseCase(any()) } returns testPost

            mockMvc.post("/api/v1/posts") {
                header("X-Member-Id", "100")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("title" to "Test Title", "content" to "Test Content", "tags" to listOf("kotlin"))
                )
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.authorId") { value(100) }
                jsonPath("$.title") { value("Test Title") }
                jsonPath("$.content") { value("Test Content") }
                jsonPath("$.tags[0]") { value("kotlin") }
                jsonPath("$.status") { value("PUBLISHED") }
                jsonPath("$.createdAt") { exists() }
                jsonPath("$.updatedAt") { exists() }
            }
        }

        it("should return 400 when title is blank") {
            mockMvc.post("/api/v1/posts") {
                header("X-Member-Id", "100")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("title" to "", "content" to "Test Content")
                )
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
            }
        }

        it("should return 400 when X-Member-Id header is missing") {
            mockMvc.post("/api/v1/posts") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("title" to "Test Title", "content" to "Test Content")
                )
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("MISSING_HEADER") }
                jsonPath("$.message") { value("Required header: X-Member-Id") }
            }
        }
    }

    describe("PUT /api/v1/posts/{postId}") {

        it("should return 200 with updated PostResponse") {
            val updatedPost = createTestPost(title = "Updated Title")
            every { updatePostUseCase(any()) } returns updatedPost

            mockMvc.put("/api/v1/posts/1") {
                header("X-Member-Id", "100")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("title" to "Updated Title")
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.title") { value("Updated Title") }
                jsonPath("$.status") { value("PUBLISHED") }
            }
        }

        it("should return 403 when non-author updates") {
            every { updatePostUseCase(any()) } throws AccessDeniedException("Only the author can edit this post")

            mockMvc.put("/api/v1/posts/1") {
                header("X-Member-Id", "999")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    mapOf("title" to "Hacked Title")
                )
            }.andExpect {
                status { isForbidden() }
                jsonPath("$.code") { value("FORBIDDEN") }
                jsonPath("$.message") { value("Only the author can edit this post") }
            }
        }
    }

    describe("DELETE /api/v1/posts/{postId}") {

        it("should return 204 on successful delete") {
            every { deletePostUseCase(any()) } just Runs

            mockMvc.delete("/api/v1/posts/1") {
                header("X-Member-Id", "100")
            }.andExpect {
                status { isNoContent() }
            }
        }

        it("should return 403 when non-author deletes") {
            every { deletePostUseCase(any()) } throws AccessDeniedException("Only the author can delete this post")

            mockMvc.delete("/api/v1/posts/1") {
                header("X-Member-Id", "999")
            }.andExpect {
                status { isForbidden() }
                jsonPath("$.code") { value("FORBIDDEN") }
                jsonPath("$.message") { value("Only the author can delete this post") }
            }
        }
    }

    describe("GET /api/v1/posts/{postId}") {

        it("should return 200 with PostDetailResponse") {
            val testPost = createTestPost()
            val stats = PostStats(
                postId = PostId(1L),
                likeCount = 10,
                commentCount = 5,
                viewCount = 200,
            )
            every { getPostUseCase(any()) } returns PostWithStats(testPost, stats)

            mockMvc.get("/api/v1/posts/1").andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.authorId") { value(100) }
                jsonPath("$.title") { value("Test Title") }
                jsonPath("$.content") { value("Test Content") }
                jsonPath("$.tags[0]") { value("kotlin") }
                jsonPath("$.status") { value("PUBLISHED") }
                jsonPath("$.likeCount") { value(10) }
                jsonPath("$.commentCount") { value(5) }
                jsonPath("$.viewCount") { value(200) }
                jsonPath("$.createdAt") { exists() }
                jsonPath("$.updatedAt") { exists() }
            }
        }

        it("should return 404 when post not found") {
            every { getPostUseCase(any()) } throws PostNotFoundException(PostId(999L))

            mockMvc.get("/api/v1/posts/999").andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("POST_NOT_FOUND") }
                jsonPath("$.message") { value("Post not found: 999") }
            }
        }
    }
})

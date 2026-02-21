package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.exception.ApiExceptionHandler
import com.anoncommunity.app.usecase.feed.GetFeedQuery
import com.anoncommunity.app.usecase.feed.GetFeedUseCase
import com.anoncommunity.app.usecase.feed.GetFollowingFeedUseCase
import com.anoncommunity.contract.model.PageResult
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class FeedControllerSpec : DescribeSpec({

    val getFeedUseCase = mockk<GetFeedUseCase>()
    val getFollowingFeedUseCase = mockk<GetFollowingFeedUseCase>()
    val controller = FeedController(getFeedUseCase, getFollowingFeedUseCase)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(ApiExceptionHandler())
        .setValidator(LocalValidatorFactoryBean().apply { afterPropertiesSet() })
        .build()

    beforeEach {
        clearMocks(getFeedUseCase)
        every { getFeedUseCase(any()) } returns PageResult(
            content = emptyList(),
            page = 0,
            size = 20,
            totalElements = 0L,
            totalPages = 0,
        )
    }

    describe("FeedController") {

        context("GET /api/v1/feed sort field validation") {

            it("should accept commentCount as a valid sort field") {
                val querySlot = slot<GetFeedQuery>()
                every { getFeedUseCase(capture(querySlot)) } returns PageResult(
                    content = emptyList(), page = 0, size = 20, totalElements = 0L, totalPages = 0
                )

                mockMvc.get("/api/v1/feed") {
                    param("sortBy", "commentCount")
                }.andExpect { status { isOk() } }

                // commentCount should be used as-is, not defaulted to createdAt
                querySlot.captured.pageable.sortBy shouldBe "commentCount"
            }

            it("should reject invalid sort field and default to createdAt") {
                val querySlot = slot<GetFeedQuery>()
                every { getFeedUseCase(capture(querySlot)) } returns PageResult(
                    content = emptyList(), page = 0, size = 20, totalElements = 0L, totalPages = 0
                )

                mockMvc.get("/api/v1/feed") {
                    param("sortBy", "invalidField")
                }.andExpect { status { isOk() } }

                querySlot.captured.pageable.sortBy shouldBe "createdAt"
            }

            it("should accept createdAt as a valid sort field") {
                val querySlot = slot<GetFeedQuery>()
                every { getFeedUseCase(capture(querySlot)) } returns PageResult(
                    content = emptyList(), page = 0, size = 20, totalElements = 0L, totalPages = 0
                )

                mockMvc.get("/api/v1/feed") {
                    param("sortBy", "createdAt")
                }.andExpect { status { isOk() } }

                querySlot.captured.pageable.sortBy shouldBe "createdAt"
            }

            it("should accept likeCount as a valid sort field") {
                val querySlot = slot<GetFeedQuery>()
                every { getFeedUseCase(capture(querySlot)) } returns PageResult(
                    content = emptyList(), page = 0, size = 20, totalElements = 0L, totalPages = 0
                )

                mockMvc.get("/api/v1/feed") {
                    param("sortBy", "likeCount")
                }.andExpect { status { isOk() } }

                querySlot.captured.pageable.sortBy shouldBe "likeCount"
            }

            it("should accept viewCount as a valid sort field") {
                val querySlot = slot<GetFeedQuery>()
                every { getFeedUseCase(capture(querySlot)) } returns PageResult(
                    content = emptyList(), page = 0, size = 20, totalElements = 0L, totalPages = 0
                )

                mockMvc.get("/api/v1/feed") {
                    param("sortBy", "viewCount")
                }.andExpect { status { isOk() } }

                querySlot.captured.pageable.sortBy shouldBe "viewCount"
            }
        }
    }
})

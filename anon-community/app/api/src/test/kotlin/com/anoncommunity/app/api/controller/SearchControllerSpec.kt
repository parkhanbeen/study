package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.exception.ApiExceptionHandler
import com.anoncommunity.app.usecase.search.CursorSearchPostQuery
import com.anoncommunity.app.usecase.search.GetHotTagsUseCase
import com.anoncommunity.app.usecase.search.SearchPostUseCase
import com.anoncommunity.domain.post.port.CursorPageResult
import com.anoncommunity.domain.post.port.PostSearchHit
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

class SearchControllerSpec : DescribeSpec({

    val searchPostUseCase = mockk<SearchPostUseCase>()
    val getHotTagsUseCase = mockk<GetHotTagsUseCase>()

    val controller = SearchController(searchPostUseCase, getHotTagsUseCase)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(ApiExceptionHandler())
        .setValidator(LocalValidatorFactoryBean().apply { afterPropertiesSet() })
        .build()

    val emptyCursorResult = CursorPageResult<PostSearchHit>(
        content = emptyList(),
        size = 20,
        searchAfter = null,
        hasNext = false,
    )

    beforeEach {
        clearMocks(searchPostUseCase, getHotTagsUseCase)
        every { searchPostUseCase.searchHitsWithCursor(any()) } returns emptyCursorResult
        every { getHotTagsUseCase(any()) } returns emptyList()
    }

    describe("SearchController") {

        context("GET /api/v1/search/posts/cursor - sortBy parameter") {
            it("should pass valid sortBy to use case") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("sortBy", "likeCount")
                }.andExpect { status { isOk() } }

                querySlot.captured.sortBy shouldBe "likeCount"
            }

            it("should fall back to createdAt when sortBy is invalid") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("sortBy", "maliciousField")
                }.andExpect { status { isOk() } }

                querySlot.captured.sortBy shouldBe "createdAt"
            }

            it("should accept commentCount as valid sortBy") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("sortBy", "commentCount")
                }.andExpect { status { isOk() } }

                querySlot.captured.sortBy shouldBe "commentCount"
            }

            it("should accept relevance as valid sortBy") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("sortBy", "relevance")
                }.andExpect { status { isOk() } }

                querySlot.captured.sortBy shouldBe "relevance"
            }
        }

        context("GET /api/v1/search/posts/cursor - dateRange parameter") {
            it("should pass today dateRange to use case") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("dateRange", "today")
                }.andExpect { status { isOk() } }

                querySlot.captured.dateRange shouldBe "today"
            }

            it("should pass this_week dateRange to use case") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("dateRange", "this_week")
                }.andExpect { status { isOk() } }

                querySlot.captured.dateRange shouldBe "this_week"
            }

            it("should reject unknown dateRange and pass null to use case") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor") {
                    param("dateRange", "last_year")
                }.andExpect { status { isOk() } }

                querySlot.captured.dateRange shouldBe null
            }

            it("should pass null when dateRange not provided") {
                val querySlot = slot<CursorSearchPostQuery>()
                every { searchPostUseCase.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                mockMvc.get("/api/v1/search/posts/cursor")
                    .andExpect { status { isOk() } }

                querySlot.captured.dateRange shouldBe null
            }
        }

        context("GET /api/v1/search/hot-tags") {
            it("should return 200 with list of tags") {
                every { getHotTagsUseCase(10) } returns listOf("kotlin", "spring", "backend")

                mockMvc.get("/api/v1/search/hot-tags")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.tags[0]") { value("kotlin") }
                        jsonPath("$.tags[1]") { value("spring") }
                        jsonPath("$.tags[2]") { value("backend") }
                    }
            }

            it("should return empty tags when use case returns empty") {
                every { getHotTagsUseCase(10) } returns emptyList()

                mockMvc.get("/api/v1/search/hot-tags")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.tags") { isArray() }
                    }
            }

            it("should pass limit parameter to use case") {
                val limitSlot = slot<Int>()
                every { getHotTagsUseCase(capture(limitSlot)) } returns listOf("kotlin")

                mockMvc.get("/api/v1/search/hot-tags") {
                    param("limit", "5")
                }.andExpect { status { isOk() } }

                limitSlot.captured shouldBe 5
            }

            it("should use default limit of 10 when not specified") {
                val limitSlot = slot<Int>()
                every { getHotTagsUseCase(capture(limitSlot)) } returns listOf("kotlin")

                mockMvc.get("/api/v1/search/hot-tags")
                    .andExpect { status { isOk() } }

                limitSlot.captured shouldBe 10
            }
        }
    }
})

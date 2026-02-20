package com.anoncommunity.app.usecase.search

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.Tag
import com.anoncommunity.domain.post.port.CursorPageResult
import com.anoncommunity.domain.post.port.CursorSearchQuery
import com.anoncommunity.domain.post.port.PostSearchHit
import com.anoncommunity.domain.post.port.PostSearchPort
import com.anoncommunity.domain.post.port.PostSearchQuery
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class SearchPostUseCaseSpec : DescribeSpec({

    val postSearchPort = mockk<PostSearchPort>()

    val useCase = SearchPostUseCase(postSearchPort)

    val testPost = Post.create(
        id = PostId(1L),
        authorId = MemberId(100L),
        title = PostTitle("Search Result"),
        content = PostContent("Search Content"),
        tags = listOf(Tag("kotlin")),
    )

    val emptyPageResult = PageResult<Post>(
        content = emptyList(),
        page = 0,
        size = 20,
        totalElements = 0L,
        totalPages = 0,
    )

    val emptyCursorResult = CursorPageResult<PostSearchHit>(
        content = emptyList(),
        size = 10,
        searchAfter = null,
        hasNext = false,
    )

    beforeEach {
        clearMocks(postSearchPort)
    }

    describe("SearchPostUseCase") {
        context("when tags are provided") {
            it("should convert tags to Tag value objects and delegate to port") {
                val pageable = DomainPageable(page = 0, size = 10)
                val expectedResult = PageResult(
                    content = listOf(testPost),
                    page = 0,
                    size = 10,
                    totalElements = 1L,
                    totalPages = 1,
                )
                val querySlot = slot<PostSearchQuery>()
                every { postSearchPort.search(capture(querySlot)) } returns expectedResult

                val result = useCase(
                    SearchPostQuery(
                        keyword = "kotlin",
                        tags = listOf("kotlin", "spring"),
                        language = "en",
                        pageable = pageable,
                    )
                )

                result shouldBe expectedResult

                val captured = querySlot.captured
                captured.keyword shouldBe "kotlin"
                captured.tags shouldBe listOf(Tag("kotlin"), Tag("spring"))
                captured.language shouldBe "en"
                captured.pageable shouldBe pageable

                verify(exactly = 1) { postSearchPort.search(any()) }
            }
        }

        context("when keyword and tags are null") {
            it("should pass null keyword and tags correctly") {
                val pageable = DomainPageable(page = 0, size = 20)
                val querySlot = slot<PostSearchQuery>()
                every { postSearchPort.search(capture(querySlot)) } returns emptyPageResult

                val result = useCase(
                    SearchPostQuery(
                        keyword = null,
                        tags = null,
                        language = null,
                        pageable = pageable,
                    )
                )

                result shouldBe emptyPageResult

                val captured = querySlot.captured
                captured.keyword shouldBe null
                captured.tags shouldBe null
                captured.language shouldBe null
                captured.pageable shouldBe pageable
            }
        }
    }

    describe("SearchPostUseCase - searchHitsWithCursor") {
        context("when sortBy and dateRange are provided") {
            it("should pass sortBy to CursorSearchQuery") {
                val querySlot = slot<CursorSearchQuery>()
                every { postSearchPort.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                useCase.searchHitsWithCursor(
                    CursorSearchPostQuery(
                        keyword = "kotlin",
                        size = 10,
                        sortBy = "likeCount",
                    )
                )

                querySlot.captured.sortBy shouldBe "likeCount"
            }

            it("should pass dateRange to CursorSearchQuery") {
                val querySlot = slot<CursorSearchQuery>()
                every { postSearchPort.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                useCase.searchHitsWithCursor(
                    CursorSearchPostQuery(
                        keyword = "kotlin",
                        size = 10,
                        dateRange = "this_week",
                    )
                )

                querySlot.captured.dateRange shouldBe "this_week"
            }

            it("should default sortBy to createdAt when not specified") {
                val querySlot = slot<CursorSearchQuery>()
                every { postSearchPort.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                useCase.searchHitsWithCursor(
                    CursorSearchPostQuery(keyword = "kotlin", size = 10)
                )

                querySlot.captured.sortBy shouldBe "createdAt"
            }

            it("should pass null dateRange when not specified") {
                val querySlot = slot<CursorSearchQuery>()
                every { postSearchPort.searchHitsWithCursor(capture(querySlot)) } returns emptyCursorResult

                useCase.searchHitsWithCursor(
                    CursorSearchPostQuery(keyword = "kotlin", size = 10)
                )

                querySlot.captured.dateRange shouldBe null
            }
        }
    }
})

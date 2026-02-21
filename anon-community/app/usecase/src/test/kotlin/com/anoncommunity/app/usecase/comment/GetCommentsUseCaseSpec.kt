package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.model.SortDirection
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.port.CommentQueryPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetCommentsUseCaseSpec : DescribeSpec({

    val commentQueryPort = mockk<CommentQueryPort>()

    val useCase = GetCommentsUseCase(commentQueryPort)

    beforeEach {
        clearMocks(commentQueryPort)
    }

    describe("GetCommentsUseCase") {

        context("when comments exist for the given postId") {
            it("should return paged comments for given postId") {
                val postId = PostId(10L)
                val pageable = DomainPageable()

                val comments = listOf(
                    Comment.create(
                        id = CommentId(1L),
                        postId = postId,
                        authorProfileId = ProfileId(100L),
                        content = CommentContent("First comment"),
                        depth = 0,
                    ),
                    Comment.create(
                        id = CommentId(2L),
                        postId = postId,
                        authorProfileId = ProfileId(200L),
                        content = CommentContent("Second comment"),
                        depth = 0,
                    ),
                )

                val expectedResult = PageResult(
                    content = comments,
                    page = 0,
                    size = 20,
                    totalElements = 2L,
                    totalPages = 1,
                )

                every { commentQueryPort.findByPostId(postId, pageable) } returns expectedResult

                val query = GetCommentsQuery(postId = 10L, pageable = pageable)
                val result = useCase(query)

                result.content.size shouldBe 2
                result.totalElements shouldBe 2L
                result.totalPages shouldBe 1
                result.page shouldBe 0
                result.size shouldBe 20

                verify(exactly = 1) { commentQueryPort.findByPostId(postId, pageable) }
            }
        }

        context("when custom pageable parameters are provided") {
            it("should pass pageable parameters correctly") {
                val postId = PostId(10L)
                val customPageable = DomainPageable(
                    page = 2,
                    size = 5,
                    sortBy = "createdAt",
                    sortDirection = SortDirection.ASC,
                )

                val expectedResult = PageResult(
                    content = emptyList<Comment>(),
                    page = 2,
                    size = 5,
                    totalElements = 12L,
                    totalPages = 3,
                )

                every { commentQueryPort.findByPostId(postId, customPageable) } returns expectedResult

                val query = GetCommentsQuery(postId = 10L, pageable = customPageable)
                val result = useCase(query)

                result.page shouldBe 2
                result.size shouldBe 5
                result.totalElements shouldBe 12L
                result.totalPages shouldBe 3
                result.content shouldBe emptyList()

                verify(exactly = 1) { commentQueryPort.findByPostId(postId, customPageable) }
            }
        }
    }
})

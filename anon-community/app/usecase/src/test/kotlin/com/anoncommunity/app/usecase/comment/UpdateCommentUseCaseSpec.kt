package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UpdateCommentUseCaseSpec : DescribeSpec({

    val commentQueryPort = mockk<CommentQueryPort>()
    val commentCommandPort = mockk<CommentCommandPort>()
    val useCase = UpdateCommentUseCase(commentQueryPort, commentCommandPort)

    val activeComment = Comment.create(
        id = CommentId(1L),
        postId = PostId(10L),
        authorProfileId = ProfileId(100L),
        content = CommentContent("Original content"),
    )

    beforeEach {
        clearMocks(commentQueryPort, commentCommandPort)
    }

    describe("UpdateCommentUseCase") {

        context("author updates own comment") {
            it("should update content and return updated comment") {
                every { commentQueryPort.findById(CommentId(1L)) } returns activeComment
                every { commentCommandPort.save(any()) } answers { firstArg() }

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 100L, content = "Updated content")
                val result = useCase(command)

                result.content shouldBe CommentContent("Updated content")
                verify(exactly = 1) { commentCommandPort.save(any()) }
            }
        }

        context("non-author tries to update") {
            it("should throw CommentNotFoundException") {
                every { commentQueryPort.findById(CommentId(1L)) } returns activeComment

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 999L, content = "Hack")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                verify(exactly = 0) { commentCommandPort.save(any()) }
            }
        }

        context("comment not found") {
            it("should throw CommentNotFoundException") {
                every { commentQueryPort.findById(CommentId(99L)) } returns null

                val command = UpdateCommentCommand(commentId = 99L, requesterId = 100L, content = "Update")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
            }
        }

        context("updating deleted comment") {
            // CommentNotFoundException: DeleteCommentUseCase와 동일한 예외 사용 (일관성 + 404 응답)
            // IllegalStateException은 HTTP 409로 매핑되어 의미상 부적절
            it("should throw CommentNotFoundException") {
                val deletedComment = Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorProfileId = ProfileId(100L),
                    content = CommentContent("Original"),
                ).apply { softDelete() }

                every { commentQueryPort.findById(CommentId(1L)) } returns deletedComment

                val command = UpdateCommentCommand(commentId = 1L, requesterId = 100L, content = "Update")
                shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                verify(exactly = 0) { commentCommandPort.save(any()) }
            }
        }
    }
})

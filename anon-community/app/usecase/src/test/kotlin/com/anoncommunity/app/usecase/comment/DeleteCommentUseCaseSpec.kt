package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.event.CommentDomainEvent
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DeleteCommentUseCaseSpec : DescribeSpec({

    val commentQueryPort = mockk<CommentQueryPort>()
    val commentCommandPort = mockk<CommentCommandPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = DeleteCommentUseCase(commentQueryPort, commentCommandPort, eventPublisher)

    beforeEach {
        clearMocks(commentQueryPort, commentCommandPort, eventPublisher)
    }

    describe("DeleteCommentUseCase") {

        context("when the requester is the author") {
            it("should soft delete and publish CommentDeleted event") {
                val existingComment = Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorProfileId = ProfileId(100L),
                    content = CommentContent("Comment to delete"),
                    depth = 0,
                )

                every { commentQueryPort.findById(CommentId(1L)) } returns existingComment
                every { commentCommandPort.save(any()) } answers { firstArg() }

                val command = DeleteCommentCommand(commentId = 1L, requesterProfileId =100L)
                useCase(command)

                existingComment.status shouldBe CommentStatus.DELETED

                verify(exactly = 1) { commentCommandPort.save(existingComment) }

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<CommentDomainEvent.CommentDeleted>()
                event.commentId shouldBe CommentId(1L)
                event.postId shouldBe PostId(10L)
                event.authorProfileId shouldBe ProfileId(100L)
            }
        }

        context("when the requester is not the author") {
            it("should throw CommentNotFoundException to avoid revealing authorship") {
                val existingComment = Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorProfileId = ProfileId(100L),
                    content = CommentContent("Someone else's comment"),
                    depth = 0,
                )

                every { commentQueryPort.findById(CommentId(1L)) } returns existingComment

                val command = DeleteCommentCommand(commentId = 1L, requesterProfileId =999L)

                val exception = shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                exception.commentId shouldBe CommentId(1L)

                verify(exactly = 0) { commentCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }

        context("when the comment does not exist") {
            it("should throw CommentNotFoundException") {
                every { commentQueryPort.findById(CommentId(999L)) } returns null

                val command = DeleteCommentCommand(commentId = 999L, requesterProfileId =100L)

                val exception = shouldThrow<CommentNotFoundException> {
                    useCase(command)
                }
                exception.commentId shouldBe CommentId(999L)

                verify(exactly = 0) { commentCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }
    }
})

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
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CreateCommentUseCaseSpec : DescribeSpec({

    val commentCommandPort = mockk<CommentCommandPort>()
    val commentQueryPort = mockk<CommentQueryPort>()
    val postQueryPort = mockk<PostQueryPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = CreateCommentUseCase(
        commentCommandPort, commentQueryPort, postQueryPort, eventPublisher,
    )

    val publishedPost = Post.create(
        id = PostId(10L),
        authorProfileId = ProfileId(200L),
        title = PostTitle("Test Post"),
        content = PostContent("Test Content"),
    )

    beforeEach {
        clearMocks(commentCommandPort, commentQueryPort, postQueryPort, eventPublisher)
    }

    describe("CreateCommentUseCase") {

        context("top-level comment on published post") {
            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns publishedPost
                every { commentCommandPort.save(any()) } answers {
                    val comment = firstArg<Comment>()
                    Comment.create(
                        id = CommentId(1L),
                        postId = comment.postId,
                        authorProfileId = comment.authorProfileId,
                        content = comment.content,
                        parentCommentId = comment.parentCommentId,
                        depth = comment.depth,
                    )
                }
            }

            it("should create with depth=0 and publish CommentCreated") {
                val command = CreateCommentCommand(
                    postId = 10L,
                    authorProfileId = 100L,
                    content = "This is a top-level comment",
                )

                val result = useCase(command)

                result.id shouldBe CommentId(1L)
                result.postId shouldBe PostId(10L)
                result.authorProfileId shouldBe ProfileId(100L)
                result.depth shouldBe 0
                result.parentCommentId shouldBe null
                result.status shouldBe CommentStatus.ACTIVE

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<CommentDomainEvent.CommentCreated>()
                event.commentId shouldBe CommentId(1L)
                event.postId shouldBe PostId(10L)
                event.authorProfileId shouldBe ProfileId(100L)
                event.parentCommentId shouldBe null
            }
        }

        context("reply to existing comment (depth=1)") {
            val parentComment = Comment.create(
                id = CommentId(50L),
                postId = PostId(10L),
                authorProfileId = ProfileId(300L),
                content = CommentContent("Parent comment"),
                depth = 0,
            )

            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns publishedPost
                every { commentQueryPort.findById(CommentId(50L)) } returns parentComment
                every { commentCommandPort.save(any()) } answers {
                    val comment = firstArg<Comment>()
                    Comment.create(
                        id = CommentId(2L),
                        postId = comment.postId,
                        authorProfileId = comment.authorProfileId,
                        content = comment.content,
                        parentCommentId = comment.parentCommentId,
                        depth = comment.depth,
                    )
                }
            }

            it("should create with depth=1") {
                val command = CreateCommentCommand(
                    postId = 10L,
                    authorProfileId = 100L,
                    content = "This is a reply",
                    parentCommentId = 50L,
                )

                val result = useCase(command)

                result.id shouldBe CommentId(2L)
                result.depth shouldBe 1
                result.parentCommentId shouldBe CommentId(50L)

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<CommentDomainEvent.CommentCreated>()
                event.parentCommentId shouldBe CommentId(50L)
            }
        }

        context("reply to deleted comment") {
            val deletedParent = Comment.create(
                id = CommentId(50L),
                postId = PostId(10L),
                authorProfileId = ProfileId(300L),
                content = CommentContent("Deleted parent"),
                depth = 0,
            ).apply { softDelete() }

            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns publishedPost
                every { commentQueryPort.findById(CommentId(50L)) } returns deletedParent
            }

            it("should throw IllegalStateException") {
                val command = CreateCommentCommand(
                    postId = 10L,
                    authorProfileId = 100L,
                    content = "Reply to deleted",
                    parentCommentId = 50L,
                )

                val exception = shouldThrow<IllegalStateException> {
                    useCase(command)
                }
                exception.message shouldBe "Cannot reply to a deleted comment"
            }
        }

        context("comment on non-PUBLISHED post") {
            val deletedPost = Post.create(
                id = PostId(10L),
                authorProfileId = ProfileId(200L),
                title = PostTitle("Deleted Post"),
                content = PostContent("Content"),
            ).apply { softDelete() }

            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns deletedPost
            }

            it("should throw IllegalStateException") {
                val command = CreateCommentCommand(
                    postId = 10L,
                    authorProfileId = 100L,
                    content = "Comment on deleted post",
                )

                val exception = shouldThrow<IllegalStateException> {
                    useCase(command)
                }
                exception.message shouldBe "Cannot comment on a post with status: ${PostStatus.DELETED}"
            }
        }

        context("post not found") {
            beforeEach {
                every { postQueryPort.findById(any()) } returns null
            }

            it("should throw PostNotFoundException") {
                val command = CreateCommentCommand(
                    postId = 999L,
                    authorProfileId = 100L,
                    content = "Comment on missing post",
                )

                shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
            }
        }
    }
})

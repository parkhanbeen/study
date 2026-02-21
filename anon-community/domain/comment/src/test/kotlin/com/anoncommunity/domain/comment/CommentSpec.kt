package com.anoncommunity.domain.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.comment.event.CommentDomainEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CommentSpec : DescribeSpec({

    describe("Comment.create") {
        it("should create a root comment with depth 0") {
            val comment = Comment.create(
                id = CommentId(1L),
                postId = PostId(10L),
                authorProfileId = ProfileId(100L),
                content = CommentContent("This is a comment"),
            )

            comment.id shouldBe CommentId(1L)
            comment.postId shouldBe PostId(10L)
            comment.authorProfileId shouldBe ProfileId(100L)
            comment.depth shouldBe 0
            comment.parentCommentId shouldBe null
            comment.status shouldBe CommentStatus.ACTIVE
        }

        it("should create a reply comment with depth 1") {
            val reply = Comment.create(
                id = CommentId(2L),
                postId = PostId(10L),
                authorProfileId = ProfileId(200L),
                content = CommentContent("This is a reply"),
                parentCommentId = CommentId(1L),
                depth = 1,
            )

            reply.depth shouldBe 1
            reply.parentCommentId shouldBe CommentId(1L)
        }

        it("should not record domain events (ID not yet assigned by DB)") {
            val comment = Comment.create(
                id = CommentId.UNSAVED,
                postId = PostId(10L),
                authorProfileId = ProfileId(100L),
                content = CommentContent("Comment content"),
            )

            comment.pullDomainEvents().shouldBeEmpty()
        }

        it("should reject depth exceeding MAX_DEPTH") {
            shouldThrow<IllegalArgumentException> {
                Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorProfileId = ProfileId(100L),
                    content = CommentContent("Too deep"),
                    parentCommentId = CommentId(99L),
                    depth = Comment.MAX_DEPTH + 1,
                )
            }
        }

        it("should reject reply without parent comment id") {
            shouldThrow<IllegalArgumentException> {
                Comment.create(
                    id = CommentId(1L),
                    postId = PostId(10L),
                    authorProfileId = ProfileId(100L),
                    content = CommentContent("Orphan reply"),
                    parentCommentId = null,
                    depth = 1,
                )
            }
        }
    }

    describe("Comment.softDelete") {
        it("should change status to DELETED and record CommentDeleted event") {
            val comment = Comment.create(
                id = CommentId(1L),
                postId = PostId(10L),
                authorProfileId = ProfileId(100L),
                content = CommentContent("Comment to delete"),
            ).also { it.pullDomainEvents() }

            comment.softDelete()

            comment.status shouldBe CommentStatus.DELETED

            val event = comment.pullDomainEvents().first()
                .shouldBeInstanceOf<CommentDomainEvent.CommentDeleted>()
            event.commentId shouldBe CommentId(1L)
            event.postId shouldBe PostId(10L)
            event.authorProfileId shouldBe ProfileId(100L)
        }

        it("should throw when deleting an already deleted comment") {
            val comment = Comment.create(
                id = CommentId(1L),
                postId = PostId(10L),
                authorProfileId = ProfileId(100L),
                content = CommentContent("Comment"),
            ).also {
                it.pullDomainEvents()
                it.softDelete()
                it.pullDomainEvents()
            }

            shouldThrow<IllegalStateException> {
                comment.softDelete()
            }
        }
    }

    describe("CommentContent") {
        it("should create valid content") {
            val content = CommentContent("Valid comment")
            content.value shouldBe "Valid comment"
        }

        it("should reject blank content") {
            shouldThrow<IllegalArgumentException> {
                CommentContent("")
            }
        }

        it("should reject content exceeding max length") {
            shouldThrow<IllegalArgumentException> {
                CommentContent("a".repeat(CommentContent.MAX_LENGTH + 1))
            }
        }
    }
})

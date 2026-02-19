package com.anoncommunity.domain.post

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.event.PostField
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

fun createPublishedPost() = Post.create(
    id = PostId(1L),
    authorId = MemberId(100L),
    title = PostTitle("Test Title"),
    content = PostContent("Test Content"),
    tags = listOf(Tag("kotlin")),
).also { it.pullDomainEvents() }

class PostSpec : DescribeSpec({

    describe("Post.create") {
        it("should create a post with PUBLISHED status") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Test Title"),
                content = PostContent("Test Content"),
                tags = listOf(Tag("kotlin"), Tag("spring")),
            )

            post.id shouldBe PostId(1L)
            post.authorId shouldBe MemberId(100L)
            post.title shouldBe PostTitle("Test Title")
            post.content shouldBe PostContent("Test Content")
            post.tags shouldBe listOf(Tag("kotlin"), Tag("spring"))
            post.status shouldBe PostStatus.PUBLISHED
        }

        it("should not record domain events (ID not yet assigned by DB)") {
            val post = Post.create(
                id = PostId.UNSAVED,
                authorId = MemberId(100L),
                title = PostTitle("Test Title"),
                content = PostContent("Test Content"),
                tags = listOf(Tag("kotlin")),
            )

            post.pullDomainEvents().shouldBeEmpty()
        }
    }

    describe("Post.edit") {
        it("should update title and record PostEdited event") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Original Title"),
                content = PostContent("Original Content"),
                tags = listOf(Tag("kotlin")),
            ).also { it.pullDomainEvents() }

            post.edit(title = PostTitle("Updated Title"))

            post.title shouldBe PostTitle("Updated Title")
            val events = post.pullDomainEvents()
            events shouldHaveSize 1

            val event = events.first().shouldBeInstanceOf<PostDomainEvent.PostEdited>()
            event.changedFields shouldBe setOf(PostField.TITLE)
        }

        it("should not record event when no fields change") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Original Title"),
                content = PostContent("Original Content"),
                tags = listOf(Tag("kotlin")),
            ).also { it.pullDomainEvents() }

            post.edit(title = PostTitle("Original Title"))

            post.pullDomainEvents().shouldBeEmpty()
        }

        it("should update multiple fields in one edit") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Original Title"),
                content = PostContent("Original Content"),
                tags = listOf(Tag("kotlin")),
            ).also { it.pullDomainEvents() }

            post.edit(
                content = PostContent("Updated Content"),
                tags = listOf(Tag("java"), Tag("spring")),
            )

            post.content shouldBe PostContent("Updated Content")
            post.tags shouldBe listOf(Tag("java"), Tag("spring"))

            val event = post.pullDomainEvents().first()
                .shouldBeInstanceOf<PostDomainEvent.PostEdited>()
            event.changedFields shouldBe setOf(PostField.CONTENT, PostField.TAGS)
        }

        it("should throw when editing a deleted post") {
            val deletedPost = Post.create(
                id = PostId(2L),
                authorId = MemberId(100L),
                title = PostTitle("Title"),
                content = PostContent("Content"),
            ).also {
                it.pullDomainEvents()
                it.softDelete()
                it.pullDomainEvents()
            }

            shouldThrow<IllegalStateException> {
                deletedPost.edit(title = PostTitle("New Title"))
            }
        }
    }

    describe("Post.softDelete") {
        it("should change status to DELETED and record PostDeleted event") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Title"),
                content = PostContent("Content"),
            ).also { it.pullDomainEvents() }

            post.softDelete()

            post.status shouldBe PostStatus.DELETED

            val event = post.pullDomainEvents().first()
                .shouldBeInstanceOf<PostDomainEvent.PostDeleted>()
            event.postId shouldBe PostId(1L)
            event.authorId shouldBe MemberId(100L)
        }

        it("should throw when deleting an already deleted post") {
            val post = Post.create(
                id = PostId(1L),
                authorId = MemberId(100L),
                title = PostTitle("Title"),
                content = PostContent("Content"),
            ).also {
                it.pullDomainEvents()
                it.softDelete()
                it.pullDomainEvents()
            }

            shouldThrow<IllegalStateException> {
                post.softDelete()
            }
        }
    }

    describe("Post.hide") {
        it("should transition PUBLISHED -> HIDDEN") {
            val post = createPublishedPost()

            post.hide()

            post.status shouldBe PostStatus.HIDDEN
        }

        it("should throw when hiding non-PUBLISHED post") {
            val post = createPublishedPost()
            post.hide()

            shouldThrow<IllegalStateException> {
                post.hide()
            }
        }
    }

    describe("Post.markForReview") {
        it("should transition PUBLISHED -> UNDER_REVIEW") {
            val post = createPublishedPost()

            post.markForReview()

            post.status shouldBe PostStatus.UNDER_REVIEW
        }

        it("should transition HIDDEN -> UNDER_REVIEW") {
            val post = createPublishedPost()
            post.hide()

            post.markForReview()

            post.status shouldBe PostStatus.UNDER_REVIEW
        }

        it("should throw when post is DELETED") {
            val post = createPublishedPost()
            post.softDelete()

            shouldThrow<IllegalStateException> {
                post.markForReview()
            }
        }
    }

    describe("Post.remove") {
        it("should transition PUBLISHED -> REMOVED") {
            val post = createPublishedPost()

            post.remove()

            post.status shouldBe PostStatus.REMOVED
        }

        it("should throw when already DELETED") {
            val post = createPublishedPost()
            post.softDelete()

            shouldThrow<IllegalStateException> {
                post.remove()
            }
        }

        it("should throw when already REMOVED") {
            val post = createPublishedPost()
            post.remove()

            shouldThrow<IllegalStateException> {
                post.remove()
            }
        }
    }

    describe("Post.restore") {
        it("should transition HIDDEN -> PUBLISHED") {
            val post = createPublishedPost()
            post.hide()

            post.restore()

            post.status shouldBe PostStatus.PUBLISHED
        }

        it("should transition UNDER_REVIEW -> PUBLISHED") {
            val post = createPublishedPost()
            post.markForReview()

            post.restore()

            post.status shouldBe PostStatus.PUBLISHED
        }

        it("should throw when post is PUBLISHED") {
            val post = createPublishedPost()

            shouldThrow<IllegalStateException> {
                post.restore()
            }
        }

        it("should throw when post is DELETED") {
            val post = createPublishedPost()
            post.softDelete()

            shouldThrow<IllegalStateException> {
                post.restore()
            }
        }
    }
})

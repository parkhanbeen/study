package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.event.PostField
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostCommandPort
import com.anoncommunity.domain.post.port.PostQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UpdatePostUseCaseSpec : DescribeSpec({

    val postQueryPort = mockk<PostQueryPort>()
    val postCommandPort = mockk<PostCommandPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = UpdatePostUseCase(postQueryPort, postCommandPort, eventPublisher)

    val authorId = ProfileId(100L)
    val postId = PostId(10L)

    beforeEach {
        clearMocks(postQueryPort, postCommandPort, eventPublisher)
    }

    describe("UpdatePostUseCase") {
        context("author updates own post") {
            it("should update title and publish PostEdited event") {
                val existingPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Original Title"),
                    content = PostContent("Original Content"),
                )

                every { postQueryPort.findById(postId) } returns existingPost
                every { postCommandPort.save(any()) } answers { firstArg() }

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Updated Title",
                )

                val result = useCase(command)

                result.title shouldBe PostTitle("Updated Title")
                result.content shouldBe PostContent("Original Content")

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<PostDomainEvent.PostEdited>()
                event.postId shouldBe postId
                event.changedFields shouldBe setOf(PostField.TITLE)
            }

            it("should skip event when no fields changed") {
                val existingPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Same Title"),
                    content = PostContent("Same Content"),
                )

                every { postQueryPort.findById(postId) } returns existingPost
                every { postCommandPort.save(any()) } answers { firstArg() }

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Same Title",
                )

                useCase(command)

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                captured.last() shouldBe emptyList()
            }
        }

        context("non-author attempts edit") {
            it("should throw AccessDeniedException") {
                val existingPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Original Title"),
                    content = PostContent("Original Content"),
                )

                every { postQueryPort.findById(postId) } returns existingPost

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = 999L,
                    title = "Hacked Title",
                )

                shouldThrow<AccessDeniedException> {
                    useCase(command)
                }

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }

        context("post not found") {
            it("should throw PostNotFoundException") {
                every { postQueryPort.findById(any()) } returns null

                val command = UpdatePostCommand(
                    postId = 999L,
                    requesterProfileId = authorId.value,
                    title = "Does Not Matter",
                )

                val exception = shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
                exception.postId shouldBe PostId(999L)

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }

        context("post is not PUBLISHED") {
            it("should throw PostNotFoundException when post is HIDDEN") {
                val hiddenPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Title"),
                    content = PostContent("Content"),
                ).also { it.hide() }

                every { postQueryPort.findById(postId) } returns hiddenPost

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Updated",
                )

                val exception = shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
                exception.postId shouldBe postId

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }

            it("should throw PostNotFoundException when post is UNDER_REVIEW") {
                val underReviewPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Title"),
                    content = PostContent("Content"),
                ).also { it.markForReview() }

                every { postQueryPort.findById(postId) } returns underReviewPost

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Updated",
                )

                val exception = shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
                exception.postId shouldBe postId

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }

            it("should throw PostNotFoundException when post is REMOVED") {
                val removedPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Title"),
                    content = PostContent("Content"),
                ).also { it.remove() }

                every { postQueryPort.findById(postId) } returns removedPost

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Updated",
                )

                val exception = shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
                exception.postId shouldBe postId

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }

            it("should throw PostNotFoundException when post is DELETED") {
                val deletedPost = Post.create(
                    id = postId,
                    authorProfileId = authorId,
                    title = PostTitle("Title"),
                    content = PostContent("Content"),
                ).also { it.softDelete() }

                every { postQueryPort.findById(postId) } returns deletedPost

                val command = UpdatePostCommand(
                    postId = postId.value,
                    requesterProfileId = authorId.value,
                    title = "Updated",
                )

                val exception = shouldThrow<PostNotFoundException> {
                    useCase(command)
                }
                exception.postId shouldBe postId

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }
    }
})

package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.event.PostDomainEvent
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

class DeletePostUseCaseSpec : DescribeSpec({

    val postQueryPort = mockk<PostQueryPort>()
    val postCommandPort = mockk<PostCommandPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = DeletePostUseCase(postQueryPort, postCommandPort, eventPublisher)

    val authorId = ProfileId(100L)
    val postId = PostId(10L)

    beforeEach {
        clearMocks(postQueryPort, postCommandPort, eventPublisher)
    }

    describe("DeletePostUseCase") {
        it("should soft delete and publish PostDeleted event") {
            val existingPost = Post.create(
                id = postId,
                authorProfileId = authorId,
                title = PostTitle("Post to Delete"),
                content = PostContent("Content to Delete"),
            )

            every { postQueryPort.findById(postId) } returns existingPost
            every { postCommandPort.save(any()) } answers { firstArg() }

            val command = DeletePostCommand(
                postId = postId.value,
                requesterProfileId =authorId.value,
            )

            useCase(command)

            existingPost.status shouldBe PostStatus.DELETED

            verify(exactly = 1) { postCommandPort.save(existingPost) }

            val captured = mutableListOf<List<DomainEvent>>()
            verify { eventPublisher.publish(capture(captured)) }

            val event = captured.last().first()
                .shouldBeInstanceOf<PostDomainEvent.PostDeleted>()
            event.postId shouldBe postId
            event.authorProfileId shouldBe authorId
        }

        it("should throw AccessDeniedException for non-author") {
            val existingPost = Post.create(
                id = postId,
                authorProfileId = authorId,
                title = PostTitle("Post to Delete"),
                content = PostContent("Content to Delete"),
            )

            every { postQueryPort.findById(postId) } returns existingPost

            val command = DeletePostCommand(
                postId = postId.value,
                requesterProfileId =999L,
            )

            shouldThrow<AccessDeniedException> {
                useCase(command)
            }

            verify(exactly = 0) { postCommandPort.save(any()) }
            verify(exactly = 0) { eventPublisher.publish(any()) }
        }

        it("should throw PostNotFoundException when not found") {
            every { postQueryPort.findById(any()) } returns null

            val command = DeletePostCommand(
                postId = 999L,
                requesterProfileId =authorId.value,
            )

            val exception = shouldThrow<PostNotFoundException> {
                useCase(command)
            }
            exception.postId shouldBe PostId(999L)

            verify(exactly = 0) { postCommandPort.save(any()) }
            verify(exactly = 0) { eventPublisher.publish(any()) }
        }
    }
})

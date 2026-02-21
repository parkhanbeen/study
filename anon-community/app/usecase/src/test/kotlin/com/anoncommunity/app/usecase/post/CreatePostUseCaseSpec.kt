package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.port.PostCommandPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CreatePostUseCaseSpec : DescribeSpec({

    val postCommandPort = mockk<PostCommandPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = CreatePostUseCase(postCommandPort, eventPublisher)

    beforeEach {
        clearMocks(postCommandPort, eventPublisher)
    }

    describe("CreatePostUseCase") {
        context("when valid command is given") {
            beforeEach {
                every { postCommandPort.save(any()) } answers {
                    val post = firstArg<Post>()
                    Post.create(
                        id = PostId(1L),
                        authorProfileId = post.authorProfileId,
                        title = post.title,
                        content = post.content,
                        category = post.category,
                        tags = post.tags,
                    )
                }
            }

            it("should create a post and return it with generated id") {
                val command = CreatePostCommand(
                    authorProfileId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                    category = "DAILY",
                    tags = listOf("kotlin", "spring"),
                )

                val result = useCase(command)

                result.id shouldBe PostId(1L)
                result.authorProfileId shouldBe ProfileId(100L)
                result.status shouldBe PostStatus.PUBLISHED
            }

            it("should publish PostCreated event using saved id") {
                val command = CreatePostCommand(
                    authorProfileId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                    category = "DAILY",
                    tags = listOf("kotlin"),
                )

                useCase(command)

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<PostDomainEvent.PostCreated>()
                event.postId shouldBe PostId(1L)
                event.authorProfileId shouldBe ProfileId(100L)
                event.title shouldBe "Test Title"
                event.tags shouldBe listOf("kotlin")
            }

            it("should save the post via PostCommandPort") {
                val command = CreatePostCommand(
                    authorProfileId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                    category = "DAILY",
                )

                useCase(command)

                verify(exactly = 1) { postCommandPort.save(any()) }
            }
        }
    }
})

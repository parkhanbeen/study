package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.port.MemberQueryPort
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.port.PostCommandPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CreatePostUseCaseSpec : DescribeSpec({

    val postCommandPort = mockk<PostCommandPort>()
    val memberQueryPort = mockk<MemberQueryPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = CreatePostUseCase(postCommandPort, memberQueryPort, eventPublisher)

    val testMember = Member.create(
        id = MemberId(100L),
        nickname = MemberNickname("testuser"),
        bio = Bio("test bio"),
    )

    beforeEach {
        clearMocks(postCommandPort, memberQueryPort, eventPublisher)
    }

    describe("CreatePostUseCase") {
        context("when member exists") {
            beforeEach {
                every { memberQueryPort.findById(MemberId(100L)) } returns testMember
                every { postCommandPort.save(any()) } answers {
                    val post = firstArg<Post>()
                    Post.create(
                        id = PostId(1L),
                        authorId = post.authorId,
                        title = post.title,
                        content = post.content,
                        tags = post.tags,
                    )
                }
            }

            it("should create a post and return it with generated id") {
                val command = CreatePostCommand(
                    authorId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                    tags = listOf("kotlin", "spring"),
                )

                val result = useCase(command)

                result.id shouldBe PostId(1L)
                result.authorId shouldBe MemberId(100L)
                result.status shouldBe PostStatus.PUBLISHED
            }

            it("should publish PostCreated event using saved id") {
                val command = CreatePostCommand(
                    authorId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                    tags = listOf("kotlin"),
                )

                useCase(command)

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<PostDomainEvent.PostCreated>()
                event.postId shouldBe PostId(1L)
                event.authorId shouldBe MemberId(100L)
                event.title shouldBe "Test Title"
                event.tags shouldBe listOf("kotlin")
            }

            it("should save the post via PostCommandPort") {
                val command = CreatePostCommand(
                    authorId = 100L,
                    title = "Test Title",
                    content = "Test Content",
                )

                useCase(command)

                verify(exactly = 1) { postCommandPort.save(any()) }
            }
        }

        context("when member is suspended") {
            it("should throw IllegalStateException") {
                val suspendedMember = Member.create(
                    id = MemberId(100L),
                    nickname = MemberNickname("testuser"),
                ).also { it.suspend() }

                every { memberQueryPort.findById(MemberId(100L)) } returns suspendedMember

                shouldThrow<IllegalStateException> {
                    useCase(CreatePostCommand(authorId = 100L, title = "Test", content = "Content"))
                }.message shouldBe "Suspended or banned members cannot create posts"

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }

        context("when member is banned") {
            it("should throw IllegalStateException") {
                val bannedMember = Member.create(
                    id = MemberId(100L),
                    nickname = MemberNickname("testuser"),
                ).also { it.ban() }

                every { memberQueryPort.findById(MemberId(100L)) } returns bannedMember

                shouldThrow<IllegalStateException> {
                    useCase(CreatePostCommand(authorId = 100L, title = "Test", content = "Content"))
                }.message shouldBe "Suspended or banned members cannot create posts"

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }

        context("when member does not exist") {
            beforeEach {
                every { memberQueryPort.findById(any()) } returns null
            }

            it("should throw MemberNotFoundException") {
                val command = CreatePostCommand(
                    authorId = 999L,
                    title = "Test Title",
                    content = "Test Content",
                )

                val exception = shouldThrow<MemberNotFoundException> {
                    useCase(command)
                }
                exception.memberId shouldBe MemberId(999L)
            }

            it("should not save post or publish events") {
                val command = CreatePostCommand(
                    authorId = 999L,
                    title = "Test Title",
                    content = "Test Content",
                )

                runCatching { useCase(command) }

                verify(exactly = 0) { postCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }
    }
})

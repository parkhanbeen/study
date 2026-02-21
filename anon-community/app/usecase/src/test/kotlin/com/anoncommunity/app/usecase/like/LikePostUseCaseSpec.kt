package com.anoncommunity.app.usecase.like

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.PostLike
import com.anoncommunity.domain.like.event.LikeDomainEvent
import com.anoncommunity.domain.like.exception.PostAlreadyLikedException
import com.anoncommunity.domain.like.port.LikeCommandPort
import com.anoncommunity.domain.like.port.LikeQueryPort
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
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

class LikePostUseCaseSpec : DescribeSpec({

    val likeCommandPort = mockk<LikeCommandPort>()
    val likeQueryPort = mockk<LikeQueryPort>()
    val postQueryPort = mockk<PostQueryPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = LikePostUseCase(likeCommandPort, likeQueryPort, postQueryPort, eventPublisher)

    val testPost = Post.create(
        id = PostId(10L),
        authorProfileId = ProfileId(200L),
        title = PostTitle("Test Post"),
        content = PostContent("Test Content"),
    )

    beforeEach {
        clearMocks(likeCommandPort, likeQueryPort, postQueryPort, eventPublisher)
    }

    describe("LikePostUseCase") {
        context("when post exists and not already liked") {
            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns testPost
                every { likeQueryPort.existsByPostIdAndProfileId(PostId(10L), ProfileId(100L)) } returns false
                every { likeCommandPort.save(any()) } answers {
                    val like = firstArg<PostLike>()
                    PostLike.like(
                        id = PostLikeId(1L),
                        postId = like.postId,
                        profileId = like.profileId,
                        postAuthorProfileId = testPost.authorProfileId,
                    )
                }
            }

            it("should create a like and return it") {
                val result = useCase(LikePostCommand(postId = 10L, profileId =100L))

                result.postId shouldBe PostId(10L)
                result.profileId shouldBe ProfileId(100L)
            }

            it("should publish PostLiked event with saved id") {
                useCase(LikePostCommand(postId = 10L, profileId =100L))

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<LikeDomainEvent.PostLiked>()
                event.postLikeId shouldBe PostLikeId(1L)
                event.postId shouldBe PostId(10L)
            }
        }

        context("when post does not exist") {
            beforeEach {
                every { postQueryPort.findById(any()) } returns null
            }

            it("should throw PostNotFoundException") {
                shouldThrow<PostNotFoundException> {
                    useCase(LikePostCommand(postId = 999L, profileId =100L))
                }
            }
        }

        context("when already liked") {
            beforeEach {
                every { postQueryPort.findById(PostId(10L)) } returns testPost
                every { likeQueryPort.existsByPostIdAndProfileId(PostId(10L), ProfileId(100L)) } returns true
            }

            it("should throw PostAlreadyLikedException") {
                shouldThrow<PostAlreadyLikedException> {
                    useCase(LikePostCommand(postId = 10L, profileId =100L))
                }
            }
        }

        context("when member tries to like own post") {
            it("should throw IllegalArgumentException for self-like") {
                val ownPost = Post.create(
                    id = PostId(10L),
                    authorProfileId = ProfileId(200L),
                    title = PostTitle("My Post"),
                    content = PostContent("My Content"),
                )

                every { postQueryPort.findById(PostId(10L)) } returns ownPost
                every { likeQueryPort.existsByPostIdAndProfileId(PostId(10L), ProfileId(200L)) } returns false

                shouldThrow<IllegalArgumentException> {
                    useCase(LikePostCommand(postId = 10L, profileId =200L))
                }.message shouldBe "Cannot like your own post"

                verify(exactly = 0) { likeCommandPort.save(any()) }
                verify(exactly = 0) { eventPublisher.publish(any()) }
            }
        }
    }
})

package com.anoncommunity.app.usecase.like

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.PostLike
import com.anoncommunity.domain.like.event.LikeDomainEvent
import com.anoncommunity.domain.like.exception.LikeNotFoundException
import com.anoncommunity.domain.like.port.LikeCommandPort
import com.anoncommunity.domain.like.port.LikeQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class UnlikePostUseCaseSpec : DescribeSpec({

    val likeQueryPort = mockk<LikeQueryPort>()
    val likeCommandPort = mockk<LikeCommandPort>()
    val eventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    val useCase = UnlikePostUseCase(likeQueryPort, likeCommandPort, eventPublisher)

    val postId = PostId(10L)
    val profileId = ProfileId(100L)
    val postLikeId = PostLikeId(1L)
    val postAuthorProfileId = ProfileId(200L)

    val testPostLike = PostLike.like(
        id = postLikeId,
        postId = postId,
        profileId = profileId,
        postAuthorProfileId = postAuthorProfileId,
    )

    beforeEach {
        clearMocks(likeQueryPort, likeCommandPort, eventPublisher)
    }

    describe("UnlikePostUseCase") {
        context("when like exists") {
            beforeEach {
                every { likeQueryPort.findByPostIdAndProfileId(postId, profileId) } returns testPostLike
                every { likeCommandPort.delete(postLikeId) } just runs
            }

            it("should delete like and publish PostUnliked event") {
                useCase(UnlikePostCommand(postId = 10L, profileId =100L))

                verify(exactly = 1) { likeCommandPort.delete(postLikeId) }

                val captured = mutableListOf<List<DomainEvent>>()
                verify { eventPublisher.publish(capture(captured)) }

                val event = captured.last().first()
                    .shouldBeInstanceOf<LikeDomainEvent.PostUnliked>()
                event.postLikeId shouldBe postLikeId
                event.postId shouldBe postId
                event.profileId shouldBe profileId
            }
        }

        context("when like does not exist") {
            beforeEach {
                every { likeQueryPort.findByPostIdAndProfileId(any(), any()) } returns null
            }

            it("should throw LikeNotFoundException") {
                val exception = shouldThrow<LikeNotFoundException> {
                    useCase(UnlikePostCommand(postId = 999L, profileId =100L))
                }
                exception.postId shouldBe PostId(999L)
                exception.profileId shouldBe ProfileId(100L)
            }
        }
    }
})

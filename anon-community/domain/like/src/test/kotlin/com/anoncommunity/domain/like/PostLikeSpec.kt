package com.anoncommunity.domain.like

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.event.LikeDomainEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PostLikeSpec : DescribeSpec({

    describe("PostLike.like") {
        it("should create a like without recording events (ID not yet assigned by DB)") {
            val like = PostLike.like(
                id = PostLikeId.UNSAVED,
                postId = PostId(10L),
                profileId = ProfileId(100L),
                postAuthorProfileId = ProfileId(200L),
            )

            like.postId shouldBe PostId(10L)
            like.profileId shouldBe ProfileId(100L)
            like.pullDomainEvents().shouldBeEmpty()
        }

        it("should reject self-like") {
            val profileId = ProfileId(100L)

            shouldThrow<IllegalArgumentException> {
                PostLike.like(
                    id = PostLikeId(1L),
                    postId = PostId(10L),
                    profileId = profileId,
                    postAuthorProfileId = profileId,
                )
            }
        }
    }

    describe("PostLike.unlike") {
        it("should record PostUnliked event") {
            val like = PostLike.like(
                id = PostLikeId(1L),
                postId = PostId(10L),
                profileId = ProfileId(100L),
                postAuthorProfileId = ProfileId(200L),
            ).also { it.pullDomainEvents() }

            like.unlike()

            val events = like.pullDomainEvents()
            events shouldHaveSize 1

            val event = events.first().shouldBeInstanceOf<LikeDomainEvent.PostUnliked>()
            event.postLikeId shouldBe PostLikeId(1L)
            event.postId shouldBe PostId(10L)
            event.profileId shouldBe ProfileId(100L)
        }
    }
})

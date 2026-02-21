package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.ProfileFollowId
import com.anoncommunity.contract.model.ProfileId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProfileFollowSpec : DescribeSpec({

    describe("ProfileFollow") {

        context("follow") {
            it("should create follow relationship") {
                val follow = ProfileFollow.follow(
                    id = ProfileFollowId(1L),
                    followerProfileId = ProfileId(1L),
                    followingProfileId = ProfileId(2L),
                )
                follow.followerProfileId shouldBe ProfileId(1L)
                follow.followingProfileId shouldBe ProfileId(2L)
                follow.createdAt shouldNotBe null
            }

            it("should reject self-follow") {
                shouldThrow<IllegalArgumentException> {
                    ProfileFollow.follow(
                        id = ProfileFollowId(1L),
                        followerProfileId = ProfileId(1L),
                        followingProfileId = ProfileId(1L),
                    )
                }
            }
        }
    }
})

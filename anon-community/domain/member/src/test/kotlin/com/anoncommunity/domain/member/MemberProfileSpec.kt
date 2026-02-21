package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MemberProfileSpec : DescribeSpec({

    describe("MemberProfile") {

        context("create") {
            it("should create default profile") {
                val profile = MemberProfile.create(
                    id = ProfileId(1L),
                    memberId = MemberId(10L),
                    nickname = MemberNickname("testuser"),
                    bio = Bio("hello"),
                    isDefault = true,
                )
                profile.id shouldBe ProfileId(1L)
                profile.memberId shouldBe MemberId(10L)
                profile.nickname.value shouldBe "testuser"
                profile.isDefault shouldBe true
                profile.postCount shouldBe 0
                profile.followerCount shouldBe 0
                profile.followingCount shouldBe 0
            }

            it("should reject blank nickname") {
                shouldThrow<IllegalArgumentException> {
                    MemberProfile.create(
                        id = ProfileId(1L),
                        memberId = MemberId(10L),
                        nickname = MemberNickname(""),
                        bio = Bio(""),
                        isDefault = false,
                    )
                }
            }
        }

        context("updateProfile") {
            it("should update nickname and bio") {
                val profile = MemberProfile.create(
                    ProfileId(1L), MemberId(10L),
                    MemberNickname("old"), Bio("old bio"), isDefault = true,
                )
                val updated = profile.updateProfile(MemberNickname("new"), Bio("new bio"))
                updated.nickname.value shouldBe "new"
                updated.bio.value shouldBe "new bio"
            }
        }

        context("postCount") {
            it("should increment postCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.incrementPostCount()
                profile.postCount shouldBe 1
            }

            it("should not decrement below 0") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.decrementPostCount()
                profile.postCount shouldBe 0
            }
        }

        context("followerCount") {
            it("should increment followerCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.incrementFollowerCount()
                profile.followerCount shouldBe 1
            }

            it("should not decrement followerCount below 0") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.decrementFollowerCount()
                profile.followerCount shouldBe 0
            }
        }

        context("followingCount") {
            it("should increment followingCount") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.incrementFollowingCount()
                profile.followingCount shouldBe 1
            }

            it("should not decrement followingCount below 0") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                profile.decrementFollowingCount()
                profile.followingCount shouldBe 0
            }
        }

        context("requireDeletable") {
            it("should pass for non-default profile") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = false)
                profile.requireDeletable()  // should not throw
            }

            it("should throw for default profile") {
                val profile = MemberProfile.create(ProfileId(1L), MemberId(10L), MemberNickname("us"), Bio(""), isDefault = true)
                shouldThrow<IllegalStateException> { profile.requireDeletable() }
            }
        }
    }
})

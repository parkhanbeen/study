package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetPublicProfileUseCaseSpec : DescribeSpec({

    val profileQueryPort = mockk<ProfileQueryPort>()
    val followQueryPort = mockk<FollowQueryPort>()

    val useCase = GetPublicProfileUseCase(profileQueryPort, followQueryPort)

    val targetProfile = MemberProfile.create(
        id = ProfileId(10L),
        memberId = MemberId(2L),
        nickname = MemberNickname("targetuser"),
        bio = Bio("target bio"),
        isDefault = true,
    )

    beforeEach {
        clearMocks(profileQueryPort, followQueryPort)
    }

    describe("GetPublicProfileUseCase") {

        context("when the requester is following the target profile") {
            it("should return PublicProfile with isFollowing = true") {
                every { profileQueryPort.findById(ProfileId(10L)) } returns targetProfile
                every {
                    followQueryPort.existsByFollowerAndFollowing(ProfileId(5L), ProfileId(10L))
                } returns true

                val result = useCase(GetPublicProfileQuery(profileId = 10L, requesterProfileId = 5L))

                result.profile.id shouldBe ProfileId(10L)
                result.isFollowing shouldBe true
            }
        }

        context("when the requester is not following the target profile") {
            it("should return PublicProfile with isFollowing = false") {
                every { profileQueryPort.findById(ProfileId(10L)) } returns targetProfile
                every {
                    followQueryPort.existsByFollowerAndFollowing(ProfileId(5L), ProfileId(10L))
                } returns false

                val result = useCase(GetPublicProfileQuery(profileId = 10L, requesterProfileId = 5L))

                result.profile.id shouldBe ProfileId(10L)
                result.isFollowing shouldBe false
            }
        }

        context("when there is no requester (anonymous access)") {
            it("should return PublicProfile with isFollowing = false without calling followQueryPort") {
                every { profileQueryPort.findById(ProfileId(10L)) } returns targetProfile

                val result = useCase(GetPublicProfileQuery(profileId = 10L, requesterProfileId = null))

                result.profile.id shouldBe ProfileId(10L)
                result.isFollowing shouldBe false
                verify { followQueryPort wasNot Called }
            }
        }

        context("when the target profile does not exist") {
            it("should throw ProfileNotFoundException") {
                every { profileQueryPort.findById(ProfileId(99L)) } returns null

                shouldThrow<ProfileNotFoundException> {
                    useCase(GetPublicProfileQuery(profileId = 99L, requesterProfileId = 5L))
                }
            }
        }
    }
})

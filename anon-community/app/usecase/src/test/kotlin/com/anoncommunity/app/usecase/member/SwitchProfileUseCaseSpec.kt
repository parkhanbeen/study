package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class SwitchProfileUseCaseSpec : DescribeSpec({

    val profileQueryPort = mockk<ProfileQueryPort>()

    val useCase = SwitchProfileUseCase(profileQueryPort)

    val ownerProfile = MemberProfile.create(
        id = ProfileId(42L),
        memberId = MemberId(1L),
        nickname = MemberNickname("myprofile"),
        bio = Bio("test bio"),
        isDefault = false,
    )

    beforeEach {
        clearMocks(profileQueryPort)
    }

    describe("SwitchProfileUseCase") {

        context("when the owner switches to their own profile") {
            it("should return the validated MemberProfile") {
                every { profileQueryPort.findById(ProfileId(42L)) } returns ownerProfile

                val result = useCase(SwitchProfileCommand(memberId = 1L, targetProfileId = 42L))

                result.id shouldBe ProfileId(42L)
                result.memberId shouldBe MemberId(1L)
            }
        }

        context("when another member tries to switch to someone else's profile") {
            it("should throw ProfileNotFoundException (no information leakage)") {
                val otherMembersProfile = MemberProfile.create(
                    id = ProfileId(42L),
                    memberId = MemberId(999L),
                    nickname = MemberNickname("otherprofile"),
                    bio = Bio("other bio"),
                    isDefault = false,
                )
                every { profileQueryPort.findById(ProfileId(42L)) } returns otherMembersProfile

                shouldThrow<ProfileNotFoundException> {
                    useCase(SwitchProfileCommand(memberId = 1L, targetProfileId = 42L))
                }
            }
        }

        context("when the target profile does not exist") {
            it("should throw ProfileNotFoundException") {
                every { profileQueryPort.findById(ProfileId(99L)) } returns null

                shouldThrow<ProfileNotFoundException> {
                    useCase(SwitchProfileCommand(memberId = 1L, targetProfileId = 99L))
                }
            }
        }
    }
})

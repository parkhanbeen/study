package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class DeleteProfileUseCaseSpec : DescribeSpec({

    val profileQueryPort = mockk<ProfileQueryPort>()
    val profileCommandPort = mockk<ProfileCommandPort>()

    val useCase = DeleteProfileUseCase(profileQueryPort, profileCommandPort)

    val nonDefaultProfile = MemberProfile.create(
        id = ProfileId(20L),
        memberId = MemberId(1L),
        nickname = MemberNickname("secondary"),
        bio = Bio("secondary profile"),
        isDefault = false,
    )

    val defaultProfile = MemberProfile.create(
        id = ProfileId(21L),
        memberId = MemberId(1L),
        nickname = MemberNickname("mainprofile"),
        bio = Bio("main profile"),
        isDefault = true,
    )

    beforeEach {
        clearMocks(profileQueryPort, profileCommandPort)
    }

    describe("DeleteProfileUseCase") {

        context("when valid non-default profile is deleted by the owner") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(20L)) } returns nonDefaultProfile
                justRun { profileCommandPort.delete(ProfileId(20L)) }
            }

            it("should call delete on the port with the profile id") {
                val command = DeleteProfileCommand(memberId = 1L, profileId = 20L)

                useCase(command)

                verify(exactly = 1) { profileCommandPort.delete(ProfileId(20L)) }
            }
        }

        context("when another member tries to delete the profile") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(20L)) } returns nonDefaultProfile
            }

            it("should throw ProfileNotFoundException") {
                val command = DeleteProfileCommand(memberId = 999L, profileId = 20L)

                shouldThrow<ProfileNotFoundException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.delete(any()) }
            }
        }

        context("when the profile is the default profile") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(21L)) } returns defaultProfile
            }

            it("should throw IllegalStateException") {
                val command = DeleteProfileCommand(memberId = 1L, profileId = 21L)

                shouldThrow<IllegalStateException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.delete(any()) }
            }
        }

        context("when the profile does not exist") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(20L)) } returns null
            }

            it("should throw ProfileNotFoundException") {
                val command = DeleteProfileCommand(memberId = 1L, profileId = 20L)

                shouldThrow<ProfileNotFoundException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.delete(any()) }
            }
        }
    }
})

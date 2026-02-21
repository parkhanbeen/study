package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UpdateProfileUseCaseSpec : DescribeSpec({

    val profileCommandPort = mockk<ProfileCommandPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()

    val useCase = UpdateProfileUseCase(profileCommandPort, profileQueryPort)

    val existingProfile = MemberProfile.create(
        id = ProfileId(10L),
        memberId = MemberId(1L),
        nickname = MemberNickname("oldnickname"),
        bio = Bio("old bio"),
        isDefault = false,
    )

    beforeEach {
        clearMocks(profileCommandPort, profileQueryPort)
    }

    describe("UpdateProfileUseCase") {

        context("when valid command is given with a new nickname") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(10L)) } returns existingProfile
                every { profileQueryPort.existsByNickname(MemberNickname("newnickname")) } returns false
                every { profileCommandPort.save(any()) } answers { firstArg() }
            }

            it("should update nickname and bio and return updated profile") {
                val command = UpdateProfileCommand(
                    memberId = 1L,
                    profileId = 10L,
                    nickname = "newnickname",
                    bio = "new bio",
                )

                val result = useCase(command)

                result.nickname shouldBe MemberNickname("newnickname")
                result.bio shouldBe Bio("new bio")
                result.id shouldBe ProfileId(10L)

                verify(exactly = 1) { profileCommandPort.save(any()) }
            }
        }

        context("when nickname is unchanged (same as current)") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(10L)) } returns existingProfile
                every { profileCommandPort.save(any()) } answers { firstArg() }
            }

            it("should update bio without checking nickname uniqueness") {
                val command = UpdateProfileCommand(
                    memberId = 1L,
                    profileId = 10L,
                    nickname = "oldnickname",
                    bio = "updated bio only",
                )

                val result = useCase(command)

                result.nickname shouldBe MemberNickname("oldnickname")
                result.bio shouldBe Bio("updated bio only")

                // existsByNickname should not be called when nickname is unchanged
                verify(exactly = 0) { profileQueryPort.existsByNickname(any()) }
                verify(exactly = 1) { profileCommandPort.save(any()) }
            }
        }

        context("when profile belongs to another member") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(10L)) } returns existingProfile
            }

            it("should throw ProfileNotFoundException") {
                val command = UpdateProfileCommand(
                    memberId = 999L,  // different memberId
                    profileId = 10L,
                    nickname = "anynickname",
                    bio = "",
                )

                shouldThrow<ProfileNotFoundException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }

        context("when new nickname is already taken by another profile") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(10L)) } returns existingProfile
                every { profileQueryPort.existsByNickname(MemberNickname("takenname")) } returns true
            }

            it("should throw NicknameDuplicatedException") {
                val command = UpdateProfileCommand(
                    memberId = 1L,
                    profileId = 10L,
                    nickname = "takenname",
                    bio = "",
                )

                shouldThrow<NicknameDuplicatedException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }

        context("when profile does not exist") {
            beforeEach {
                every { profileQueryPort.findById(ProfileId(10L)) } returns null
            }

            it("should throw ProfileNotFoundException") {
                val command = UpdateProfileCommand(
                    memberId = 1L,
                    profileId = 10L,
                    nickname = "anynickname",
                    bio = "",
                )

                shouldThrow<ProfileNotFoundException> {
                    useCase(command)
                }

                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }
    }
})

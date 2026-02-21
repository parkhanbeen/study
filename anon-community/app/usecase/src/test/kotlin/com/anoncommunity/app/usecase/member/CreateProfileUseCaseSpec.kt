package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.exception.ProfileLimitExceededException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CreateProfileUseCaseSpec : DescribeSpec({

    val profileCommandPort = mockk<ProfileCommandPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()

    val useCase = CreateProfileUseCase(profileCommandPort, profileQueryPort)

    beforeEach {
        clearMocks(profileCommandPort, profileQueryPort)
    }

    describe("CreateProfileUseCase") {

        context("when valid command is given") {
            beforeEach {
                every { profileQueryPort.countByMemberId(any()) } returns 2
                every { profileQueryPort.existsByNickname(any()) } returns false
                every { profileCommandPort.save(any()) } answers {
                    val profile = firstArg<MemberProfile>()
                    MemberProfile.create(
                        id = ProfileId(10L),
                        memberId = profile.memberId,
                        nickname = profile.nickname,
                        bio = profile.bio,
                        isDefault = false,
                    )
                }
            }

            it("should create and save a new non-default profile") {
                val command = CreateProfileCommand(
                    memberId = 1L,
                    nickname = "newprofile",
                    bio = "My secondary profile",
                )

                val result = useCase(command)

                result.memberId shouldBe MemberId(1L)
                result.nickname shouldBe MemberNickname("newprofile")
                result.bio shouldBe Bio("My secondary profile")
                result.isDefault shouldBe false

                verify(exactly = 1) { profileCommandPort.save(match { !it.isDefault }) }
            }

            it("should pass UNSAVED id to save and return saved profile with real id") {
                val command = CreateProfileCommand(memberId = 1L, nickname = "newprofile")

                every { profileCommandPort.save(any()) } answers {
                    val profile = firstArg<MemberProfile>()
                    profile.id shouldBe ProfileId.UNSAVED
                    MemberProfile.create(
                        id = ProfileId(99L),
                        memberId = profile.memberId,
                        nickname = profile.nickname,
                        bio = profile.bio,
                        isDefault = false,
                    )
                }

                val result = useCase(command)
                result.id shouldBe ProfileId(99L)
            }
        }

        context("when member already has 5 profiles") {
            beforeEach {
                every { profileQueryPort.countByMemberId(any()) } returns 5
            }

            it("should throw ProfileLimitExceededException without saving") {
                shouldThrow<ProfileLimitExceededException> {
                    useCase(CreateProfileCommand(memberId = 1L, nickname = "sixthprofile"))
                }

                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }

        context("when nickname is already taken") {
            beforeEach {
                every { profileQueryPort.countByMemberId(any()) } returns 1
                every { profileQueryPort.existsByNickname(any()) } returns true
            }

            it("should throw NicknameDuplicatedException without saving") {
                shouldThrow<NicknameDuplicatedException> {
                    useCase(CreateProfileCommand(memberId = 1L, nickname = "takenname"))
                }

                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }
    }
})

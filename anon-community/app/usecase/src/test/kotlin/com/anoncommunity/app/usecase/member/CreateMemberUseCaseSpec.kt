package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class CreateMemberUseCaseSpec : DescribeSpec({

    val memberCommandPort = mockk<MemberCommandPort>()
    val profileCommandPort = mockk<ProfileCommandPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()
    val passwordEncoder = mockk<PasswordEncoder>()

    val useCase = CreateMemberUseCase(memberCommandPort, profileCommandPort, profileQueryPort, passwordEncoder)

    beforeEach {
        clearMocks(memberCommandPort, profileCommandPort, profileQueryPort, passwordEncoder)
    }

    describe("CreateMemberUseCase") {
        context("when valid command is given") {
            beforeEach {
                every { profileQueryPort.existsByNickname(any()) } returns false
                every { passwordEncoder.encode("rawPass123") } returns "encoded-hash"
                every { memberCommandPort.save(any()) } answers {
                    val m = firstArg<Member>()
                    Member.create(
                        id = MemberId(1L),
                        passwordHash = m.passwordHash,
                    )
                }
                every { profileCommandPort.save(any()) } returns mockk<MemberProfile>()
            }

            it("should create member with encoded password") {
                val command = CreateMemberCommand(
                    nickname = "testuser",
                    password = "rawPass123",
                    bio = "hello",
                )

                val result = useCase(command)

                result.passwordHash shouldBe "encoded-hash"
                result.id shouldNotBe MemberId.UNSAVED

                verify(exactly = 1) { passwordEncoder.encode("rawPass123") }
                verify(exactly = 1) { memberCommandPort.save(any()) }
            }

            it("should pass UNSAVED id to Member.create and receive real id after save") {
                val command = CreateMemberCommand(
                    nickname = "testuser",
                    password = "rawPass123",
                )

                every { memberCommandPort.save(any()) } answers {
                    val m = firstArg<Member>()
                    m.id shouldBe MemberId.UNSAVED
                    Member.create(
                        id = MemberId(42L),
                        passwordHash = m.passwordHash,
                    )
                }

                val result = useCase(command)

                result.id shouldBe MemberId(42L)
            }

            it("should create default profile when member is created") {
                val command = CreateMemberCommand(
                    nickname = "testuser",
                    password = "rawPass123",
                )

                useCase(command)

                verify(exactly = 1) {
                    profileCommandPort.save(match { it.isDefault && it.nickname.value == "testuser" })
                }
            }

            it("should propagate exception when profile save fails") {
                every { profileCommandPort.save(any()) } throws RuntimeException("DB error")

                shouldThrow<RuntimeException> {
                    useCase.invoke(CreateMemberCommand(nickname = "testuser", password = "rawPass123"))
                }
            }
        }

        context("when nickname is already taken") {
            beforeEach {
                every { profileQueryPort.existsByNickname(any()) } returns true
            }

            it("should throw NicknameDuplicatedException and not save member or profile") {
                shouldThrow<NicknameDuplicatedException> {
                    useCase.invoke(CreateMemberCommand(nickname = "testuser", password = "rawPass123"))
                }

                verify(exactly = 0) { memberCommandPort.save(any()) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }
    }
})

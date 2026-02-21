package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.domain.member.port.ProfileCommandPort
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
    val passwordEncoder = mockk<PasswordEncoder>()

    val useCase = CreateMemberUseCase(memberCommandPort, profileCommandPort, passwordEncoder)

    beforeEach {
        clearMocks(memberCommandPort, profileCommandPort, passwordEncoder)
    }

    describe("CreateMemberUseCase") {
        context("when valid command is given") {
            beforeEach {
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
        }
    }
})

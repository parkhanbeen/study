package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.port.MemberQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder

class LoginUseCaseSpec : DescribeSpec({

    val memberQueryPort = mockk<MemberQueryPort>()
    val passwordEncoder = mockk<PasswordEncoder>()

    val useCase = LoginUseCase(memberQueryPort, passwordEncoder)

    beforeEach {
        clearMocks(memberQueryPort, passwordEncoder)
    }

    describe("LoginUseCase") {
        it("should return member on valid credentials") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed-pw",
            )
            every { memberQueryPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches("correctPass", "hashed-pw") } returns true

            val result = useCase(LoginCommand(nickname = "testuser", password = "correctPass"))

            result.id shouldBe MemberId(1L)
        }

        it("should throw InvalidCredentialsException when member not found") {
            every { memberQueryPort.findByNickname(any()) } returns null

            shouldThrow<InvalidCredentialsException> {
                useCase(LoginCommand(nickname = "unknown", password = "anyPass"))
            }
        }

        it("should throw InvalidCredentialsException on wrong password") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed-pw",
            )
            every { memberQueryPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches("wrongPass", "hashed-pw") } returns false

            shouldThrow<InvalidCredentialsException> {
                useCase(LoginCommand(nickname = "testuser", password = "wrongPass"))
            }
        }

        it("should throw InvalidCredentialsException when passwordHash is null") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = null,
            )
            every { memberQueryPort.findByNickname(any()) } returns member

            shouldThrow<InvalidCredentialsException> {
                useCase(LoginCommand(nickname = "testuser", password = "anyPass"))
            }
        }

        it("suspended 계정은 InvalidCredentialsException을 던진다") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed-pw",
            )
            member.suspend()

            every { memberQueryPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches("correctPass", "hashed-pw") } returns true

            shouldThrow<InvalidCredentialsException> {
                useCase(LoginCommand(nickname = "testuser", password = "correctPass"))
            }
        }

        it("banned 계정은 InvalidCredentialsException을 던진다") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed-pw",
            )
            member.ban()

            every { memberQueryPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches("correctPass", "hashed-pw") } returns true

            shouldThrow<InvalidCredentialsException> {
                useCase(LoginCommand(nickname = "testuser", password = "correctPass"))
            }
        }

        it("비활성화된 계정은 InvalidCredentialsException을 던진다") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed-pw",
            ).also { it.suspend() }

            every { memberQueryPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches("pass", "hashed-pw") } returns true

            shouldThrow<InvalidCredentialsException> {
                useCase.invoke(LoginCommand(nickname = "inactive", password = "pass"))
            }
        }
    }
})

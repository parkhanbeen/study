package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.port.MemberQueryPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

data class LoginCommand(
    val nickname: String,
    val password: String,
)

@Service
class LoginUseCase(
    private val memberQueryPort: MemberQueryPort,
    private val passwordEncoder: PasswordEncoder,
) : CommandUseCase<LoginCommand, Member> {

    override fun invoke(command: LoginCommand): Member {
        val member = memberQueryPort.findByNickname(MemberNickname(command.nickname))
            ?: throw InvalidCredentialsException()

        if (member.passwordHash == null || !passwordEncoder.matches(command.password, member.passwordHash)) {
            throw InvalidCredentialsException()
        }

        if (!member.isActive) throw InvalidCredentialsException()

        return member
    }
}

class InvalidCredentialsException : RuntimeException("Invalid credentials")

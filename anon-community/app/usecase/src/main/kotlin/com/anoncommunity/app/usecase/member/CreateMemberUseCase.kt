package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateMemberCommand(
    val nickname: String,
    val password: String,
    val bio: String = "",
)

@Service
class CreateMemberUseCase(
    private val memberCommandPort: MemberCommandPort,
    private val profileCommandPort: ProfileCommandPort,
    private val profileQueryPort: ProfileQueryPort,
    private val passwordEncoder: PasswordEncoder,
) : CommandUseCase<CreateMemberCommand, Member> {

    @Transactional
    override fun invoke(command: CreateMemberCommand): Member {
        val nickname = MemberNickname(command.nickname)

        if (profileQueryPort.existsByNickname(nickname)) {
            throw NicknameDuplicatedException(nickname)
        }

        val member = Member.create(
            id = MemberId.UNSAVED,
            passwordHash = passwordEncoder.encode(command.password),
        )

        val savedMember = memberCommandPort.save(member)

        profileCommandPort.save(
            MemberProfile.create(
                id = ProfileId.UNSAVED,
                memberId = savedMember.id,
                nickname = nickname,
                bio = Bio(command.bio),
                isDefault = true,
            )
        )

        return savedMember
    }
}

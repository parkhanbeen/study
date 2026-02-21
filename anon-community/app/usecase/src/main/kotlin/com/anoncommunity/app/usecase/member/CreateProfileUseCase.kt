package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.exception.ProfileLimitExceededException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateProfileCommand(
    val memberId: Long,
    val nickname: String,
    val bio: String = "",
)

@Service
class CreateProfileUseCase(
    private val profileCommandPort: ProfileCommandPort,
    private val profileQueryPort: ProfileQueryPort,
) : CommandUseCase<CreateProfileCommand, MemberProfile> {

    @Transactional
    override fun invoke(command: CreateProfileCommand): MemberProfile {
        val memberId = MemberId(command.memberId)
        val nickname = MemberNickname(command.nickname)

        if (profileQueryPort.countByMemberId(memberId) >= MAX_PROFILES_PER_MEMBER) {
            throw ProfileLimitExceededException(MAX_PROFILES_PER_MEMBER)
        }

        if (profileQueryPort.existsByNickname(nickname)) {
            throw NicknameDuplicatedException(nickname)
        }

        return profileCommandPort.save(
            MemberProfile.create(
                id = ProfileId.UNSAVED,
                memberId = memberId,
                nickname = nickname,
                bio = Bio(command.bio),
                isDefault = false,
            )
        )
    }

    companion object {
        const val MAX_PROFILES_PER_MEMBER = 5
    }
}

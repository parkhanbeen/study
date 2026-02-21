package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdateProfileCommand(
    val memberId: Long,
    val profileId: Long,
    val nickname: String,
    val bio: String = "",
)

@Service
class UpdateProfileUseCase(
    private val profileCommandPort: ProfileCommandPort,
    private val profileQueryPort: ProfileQueryPort,
) : CommandUseCase<UpdateProfileCommand, MemberProfile> {

    @Transactional
    override fun invoke(command: UpdateProfileCommand): MemberProfile {
        val profileId = ProfileId(command.profileId)
        val memberId = MemberId(command.memberId)
        val nickname = MemberNickname(command.nickname)

        val profile = profileQueryPort.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        if (profile.memberId != memberId) {
            throw ProfileNotFoundException(profileId)
        }

        val isNicknameChanged = profile.nickname.value != command.nickname
        if (isNicknameChanged && profileQueryPort.existsByNickname(nickname)) {
            throw NicknameDuplicatedException(nickname)
        }

        return profileCommandPort.save(profile.updateProfile(nickname, Bio(command.bio)))
    }
}

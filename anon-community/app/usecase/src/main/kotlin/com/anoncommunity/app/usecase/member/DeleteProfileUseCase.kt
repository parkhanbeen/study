package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class DeleteProfileCommand(
    val memberId: Long,
    val profileId: Long,
)

@Service
class DeleteProfileUseCase(
    private val profileQueryPort: ProfileQueryPort,
    private val profileCommandPort: ProfileCommandPort,
) : CommandUseCase<DeleteProfileCommand, Unit> {

    @Transactional
    override fun invoke(command: DeleteProfileCommand) {
        val profileId = ProfileId(command.profileId)
        val memberId = MemberId(command.memberId)

        val profile = profileQueryPort.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        if (profile.memberId != memberId) {
            throw ProfileNotFoundException(profileId)
        }

        profile.requireDeletable()

        profileCommandPort.delete(profile.id)
    }
}

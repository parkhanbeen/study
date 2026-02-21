package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class SwitchProfileCommand(
    val memberId: Long,
    val targetProfileId: Long,
)

/**
 * Validates that the requesting member owns the target profile and returns it.
 *
 * JWT re-issuance with the new activeProfileId is intentionally handled in the
 * API/Gateway layer (AuthController → JwtProvider) to avoid a layer violation —
 * JwtProvider lives in app/gateway and must NOT be injected into app/usecase.
 */
@Service
@Transactional(readOnly = true)
class SwitchProfileUseCase(
    private val profileQueryPort: ProfileQueryPort,
) : CommandUseCase<SwitchProfileCommand, MemberProfile> {

    override fun invoke(command: SwitchProfileCommand): MemberProfile {
        val profileId = ProfileId(command.targetProfileId)

        val profile = profileQueryPort.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        // Use ProfileNotFoundException (not AccessDeniedException) to avoid
        // leaking information about the existence of profiles owned by other members.
        if (profile.memberId != MemberId(command.memberId)) {
            throw ProfileNotFoundException(profileId)
        }

        return profile
    }
}

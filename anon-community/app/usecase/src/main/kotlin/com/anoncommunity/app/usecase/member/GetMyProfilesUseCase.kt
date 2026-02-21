package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetMyProfilesQuery(val memberId: Long)

@Service
@Transactional(readOnly = true)
class GetMyProfilesUseCase(
    private val profileQueryPort: ProfileQueryPort,
) : QueryUseCase<GetMyProfilesQuery, List<MemberProfile>> {

    override fun invoke(query: GetMyProfilesQuery): List<MemberProfile> {
        return profileQueryPort.findByMemberId(MemberId(query.memberId))
    }
}

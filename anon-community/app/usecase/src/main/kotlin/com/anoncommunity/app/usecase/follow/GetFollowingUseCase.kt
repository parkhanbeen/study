package com.anoncommunity.app.usecase.follow

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.FollowQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetFollowingQuery(val profileId: Long, val pageable: DomainPageable = DomainPageable())

@Service
@Transactional(readOnly = true)
class GetFollowingUseCase(
    private val followQueryPort: FollowQueryPort,
) : QueryUseCase<GetFollowingQuery, PageResult<MemberProfile>> {
    override fun invoke(query: GetFollowingQuery): PageResult<MemberProfile> {
        return followQueryPort.findFollowing(ProfileId(query.profileId), query.pageable)
    }
}

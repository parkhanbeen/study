package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetPublicProfileQuery(
    val profileId: Long,
    /** Null for anonymous (unauthenticated) access — isFollowing will be false. */
    val requesterProfileId: Long?,
)

data class PublicProfile(
    val profile: MemberProfile,
    val isFollowing: Boolean,
)

@Service
@Transactional(readOnly = true)
class GetPublicProfileUseCase(
    private val profileQueryPort: ProfileQueryPort,
    private val followQueryPort: FollowQueryPort,
) : QueryUseCase<GetPublicProfileQuery, PublicProfile> {

    override fun invoke(query: GetPublicProfileQuery): PublicProfile {
        val profileId = ProfileId(query.profileId)

        val profile = profileQueryPort.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        val isFollowing = query.requesterProfileId?.let { requesterId ->
            followQueryPort.existsByFollowerAndFollowing(
                followerProfileId = ProfileId(requesterId),
                followingProfileId = profileId,
            )
        } ?: false

        return PublicProfile(profile = profile, isFollowing = isFollowing)
    }
}

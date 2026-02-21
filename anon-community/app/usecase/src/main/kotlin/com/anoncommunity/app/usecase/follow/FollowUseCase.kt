package com.anoncommunity.app.usecase.follow

import com.anoncommunity.contract.model.ProfileFollowId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.ProfileFollow
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.FollowCommandPort
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.FollowingIdsCache
import com.anoncommunity.domain.member.port.ProfileQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class FollowCommand(
    val followerProfileId: Long,
    val followingProfileId: Long,
)

@Service
class FollowUseCase(
    private val followCommandPort: FollowCommandPort,
    private val followQueryPort: FollowQueryPort,
    private val profileQueryPort: ProfileQueryPort,
    private val followingIdsCache: FollowingIdsCache,
) : CommandUseCase<FollowCommand, Unit> {

    @Transactional
    override fun invoke(command: FollowCommand) {
        require(command.followerProfileId != command.followingProfileId) { "Cannot follow yourself" }

        val followingProfileId = ProfileId(command.followingProfileId)
        profileQueryPort.findById(followingProfileId)
            ?: throw ProfileNotFoundException(followingProfileId)

        val followerProfileId = ProfileId(command.followerProfileId)
        if (followQueryPort.existsByFollowerAndFollowing(followerProfileId, followingProfileId)) return

        followCommandPort.save(
            ProfileFollow.follow(
                id = ProfileFollowId.UNSAVED,
                followerProfileId = followerProfileId,
                followingProfileId = followingProfileId,
            )
        )
        followingIdsCache.invalidate(followerProfileId)
    }
}

package com.anoncommunity.app.usecase.follow

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.port.FollowCommandPort
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.FollowingIdsCache
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UnfollowCommand(
    val followerProfileId: Long,
    val followingProfileId: Long,
)

@Service
class UnfollowUseCase(
    private val followCommandPort: FollowCommandPort,
    private val followQueryPort: FollowQueryPort,
    private val followingIdsCache: FollowingIdsCache,
) : CommandUseCase<UnfollowCommand, Unit> {

    @Transactional
    override fun invoke(command: UnfollowCommand) {
        val followerProfileId = ProfileId(command.followerProfileId)
        val followingProfileId = ProfileId(command.followingProfileId)

        if (!followQueryPort.existsByFollowerAndFollowing(followerProfileId, followingProfileId)) return

        followCommandPort.delete(followerProfileId, followingProfileId)
        followingIdsCache.invalidate(followerProfileId)
    }
}

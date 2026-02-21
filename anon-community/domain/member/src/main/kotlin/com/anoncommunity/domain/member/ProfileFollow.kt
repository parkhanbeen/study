package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.ProfileFollowId
import com.anoncommunity.contract.model.ProfileId
import java.time.Instant

class ProfileFollow private constructor(
    val id: ProfileFollowId,
    val followerProfileId: ProfileId,
    val followingProfileId: ProfileId,
    val createdAt: Instant,
) {
    companion object {
        fun follow(
            id: ProfileFollowId,
            followerProfileId: ProfileId,
            followingProfileId: ProfileId,
        ): ProfileFollow {
            require(followerProfileId != followingProfileId) { "Cannot follow yourself" }
            return ProfileFollow(id, followerProfileId, followingProfileId, Instant.now())
        }
    }
}

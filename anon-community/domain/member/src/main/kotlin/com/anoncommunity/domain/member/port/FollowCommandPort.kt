package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.ProfileFollow

interface FollowCommandPort {
    fun save(follow: ProfileFollow): ProfileFollow
    fun delete(followerProfileId: ProfileId, followingProfileId: ProfileId)
}

package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.ProfileId

interface FollowingIdsCache {
    fun get(profileId: ProfileId): List<Long>?       // null = cache miss
    fun set(profileId: ProfileId, ids: List<Long>)   // TTL 10분
    fun invalidate(profileId: ProfileId)              // Follow/Unfollow 시 무효화
}

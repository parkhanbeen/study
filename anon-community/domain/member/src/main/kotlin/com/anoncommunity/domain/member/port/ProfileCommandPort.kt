package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile

interface ProfileCommandPort {
    fun save(profile: MemberProfile): MemberProfile
    fun delete(id: ProfileId)
    fun incrementFollowerCount(profileId: ProfileId): Int
    fun decrementFollowerCount(profileId: ProfileId): Int
    fun incrementFollowingCount(profileId: ProfileId): Int
    fun decrementFollowingCount(profileId: ProfileId): Int
    fun incrementPostCount(profileId: ProfileId): Int
    fun decrementPostCount(profileId: ProfileId): Int
}

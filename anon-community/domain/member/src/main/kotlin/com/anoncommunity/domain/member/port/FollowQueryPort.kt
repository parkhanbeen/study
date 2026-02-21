package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.ProfileFollow

interface FollowQueryPort {
    fun findByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): ProfileFollow?
    fun existsByFollowerAndFollowing(followerProfileId: ProfileId, followingProfileId: ProfileId): Boolean
    fun findFollowingIds(followerProfileId: ProfileId, limit: Int = 1000): List<ProfileId>
    fun findFollowers(followingProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile>
    fun findFollowing(followerProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile>
}

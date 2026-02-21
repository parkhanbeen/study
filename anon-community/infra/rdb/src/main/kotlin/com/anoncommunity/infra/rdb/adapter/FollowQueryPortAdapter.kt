package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.ProfileFollow
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.infra.rdb.repository.ProfileFollowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class FollowQueryPortAdapter(
    private val repository: ProfileFollowRepository,
) : FollowQueryPort {

    override fun findByFollowerAndFollowing(
        followerProfileId: ProfileId,
        followingProfileId: ProfileId,
    ): ProfileFollow? =
        repository.findByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )?.toModel()

    override fun existsByFollowerAndFollowing(
        followerProfileId: ProfileId,
        followingProfileId: ProfileId,
    ): Boolean =
        repository.existsByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )

    override fun findFollowingIds(followerProfileId: ProfileId, limit: Int): List<ProfileId> =
        repository.findFollowingIds(followerProfileId.value, PageRequest.of(0, limit))
            .map { ProfileId(it) }

    override fun findFollowers(followingProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile> {
        val page = repository.findFollowerProfiles(
            followingProfileId.value,
            PageRequest.of(pageable.page, pageable.size),
        )
        return PageResult(
            content = page.content.map { it.toModel() },
            page = pageable.page,
            size = pageable.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }

    override fun findFollowing(followerProfileId: ProfileId, pageable: DomainPageable): PageResult<MemberProfile> {
        val page = repository.findFollowingProfiles(
            followerProfileId.value,
            PageRequest.of(pageable.page, pageable.size),
        )
        return PageResult(
            content = page.content.map { it.toModel() },
            page = pageable.page,
            size = pageable.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}

package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.MemberProfileEntity
import com.anoncommunity.infra.rdb.entity.ProfileFollowEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProfileFollowRepository : JpaRepository<ProfileFollowEntity, Long> {
    fun findByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    ): ProfileFollowEntity?

    fun existsByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    ): Boolean

    @Query(
        "SELECT e.followingProfileId FROM ProfileFollowEntity e " +
        "WHERE e.followerProfileId = :followerProfileId ORDER BY e.createdAt DESC",
    )
    fun findFollowingIds(followerProfileId: Long, pageable: Pageable): List<Long>

    fun deleteByFollowerProfileIdAndFollowingProfileId(
        followerProfileId: Long,
        followingProfileId: Long,
    )

    @Query(
        "SELECT p FROM MemberProfileEntity p JOIN ProfileFollowEntity f ON f.followerProfileId = p.id " +
        "WHERE f.followingProfileId = :followingProfileId",
    )
    fun findFollowerProfiles(followingProfileId: Long, pageable: Pageable): Page<MemberProfileEntity>

    @Query(
        "SELECT p FROM MemberProfileEntity p JOIN ProfileFollowEntity f ON f.followingProfileId = p.id " +
        "WHERE f.followerProfileId = :followerProfileId",
    )
    fun findFollowingProfiles(followerProfileId: Long, pageable: Pageable): Page<MemberProfileEntity>
}

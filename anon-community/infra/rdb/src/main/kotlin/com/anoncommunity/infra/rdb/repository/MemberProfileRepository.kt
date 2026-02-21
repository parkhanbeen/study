package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.MemberProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberProfileRepository : JpaRepository<MemberProfileEntity, Long> {
    fun findByMemberId(memberId: Long): List<MemberProfileEntity>
    fun findByMemberIdAndIsDefaultTrue(memberId: Long): MemberProfileEntity?
    fun countByMemberId(memberId: Long): Int
    fun findByNickname(nickname: String): MemberProfileEntity?
    fun existsByNickname(nickname: String): Boolean

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.followerCount = GREATEST(0, p.followerCount + 1) WHERE p.id = :id")
    fun incrementFollowerCount(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.followerCount = GREATEST(0, p.followerCount - 1) WHERE p.id = :id")
    fun decrementFollowerCount(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.followingCount = GREATEST(0, p.followingCount + 1) WHERE p.id = :id")
    fun incrementFollowingCount(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.followingCount = GREATEST(0, p.followingCount - 1) WHERE p.id = :id")
    fun decrementFollowingCount(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.postCount = GREATEST(0, p.postCount + 1) WHERE p.id = :id")
    fun incrementPostCount(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE MemberProfileEntity p SET p.postCount = GREATEST(0, p.postCount - 1) WHERE p.id = :id")
    fun decrementPostCount(@Param("id") id: Long): Int
}

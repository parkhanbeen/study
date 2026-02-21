package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.MemberProfileEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberProfileRepository : JpaRepository<MemberProfileEntity, Long> {
    fun findByMemberId(memberId: Long): List<MemberProfileEntity>
    fun findByMemberIdAndIsDefaultTrue(memberId: Long): MemberProfileEntity?
    fun countByMemberId(memberId: Long): Int
    fun findByNickname(nickname: String): MemberProfileEntity?
    fun existsByNickname(nickname: String): Boolean
}

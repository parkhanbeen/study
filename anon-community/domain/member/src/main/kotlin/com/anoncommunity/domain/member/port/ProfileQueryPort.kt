package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile

interface ProfileQueryPort {
    fun findById(id: ProfileId): MemberProfile?
    fun findByMemberId(memberId: MemberId): List<MemberProfile>
    fun findDefaultByMemberId(memberId: MemberId): MemberProfile?
    fun countByMemberId(memberId: MemberId): Int
    fun existsByNickname(nickname: String): Boolean
}

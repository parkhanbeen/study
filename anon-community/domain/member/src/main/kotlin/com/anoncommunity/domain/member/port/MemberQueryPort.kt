package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname

interface MemberQueryPort {
    fun findById(id: MemberId): Member?
    fun findByNickname(nickname: MemberNickname): Member?
}

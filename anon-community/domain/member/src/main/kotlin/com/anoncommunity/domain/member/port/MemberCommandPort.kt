package com.anoncommunity.domain.member.port

import com.anoncommunity.domain.member.Member

interface MemberCommandPort {
    fun save(member: Member): Member
}

package com.anoncommunity.domain.member.port

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile

interface ProfileCommandPort {
    fun save(profile: MemberProfile): MemberProfile
    fun delete(id: ProfileId)
}

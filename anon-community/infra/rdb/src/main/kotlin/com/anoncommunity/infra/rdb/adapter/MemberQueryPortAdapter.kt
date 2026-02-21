package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.port.MemberQueryPort
import com.anoncommunity.infra.rdb.repository.MemberJpaRepository
import com.anoncommunity.infra.rdb.repository.MemberProfileRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberQueryPortAdapter(
    private val memberJpaRepository: MemberJpaRepository,
    private val memberProfileRepository: MemberProfileRepository,
) : MemberQueryPort {

    override fun findById(id: MemberId): Member? =
        memberJpaRepository.findByIdOrNull(id.value)?.toModel()

    /**
     * Nickname now lives in member_profiles. Find the profile by nickname,
     * then load the owning member.
     */
    override fun findByNickname(nickname: MemberNickname): Member? {
        val profile = memberProfileRepository.findByNickname(nickname.value) ?: return null
        return memberJpaRepository.findByIdOrNull(profile.memberId)?.toModel()
    }
}

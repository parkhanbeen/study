package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.ProfileQueryPort
import com.anoncommunity.infra.rdb.repository.MemberProfileRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ProfileQueryPortAdapter(
    private val repository: MemberProfileRepository,
) : ProfileQueryPort {

    override fun findById(id: ProfileId): MemberProfile? =
        repository.findByIdOrNull(id.value)?.toModel()

    override fun findByMemberId(memberId: MemberId): List<MemberProfile> =
        repository.findByMemberId(memberId.value).map { it.toModel() }

    override fun findDefaultByMemberId(memberId: MemberId): MemberProfile? =
        repository.findByMemberIdAndIsDefaultTrue(memberId.value)?.toModel()

    override fun countByMemberId(memberId: MemberId): Int =
        repository.countByMemberId(memberId.value)

    override fun existsByNickname(nickname: MemberNickname): Boolean =
        repository.existsByNickname(nickname.value)
}

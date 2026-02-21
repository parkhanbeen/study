package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.infra.rdb.entity.MemberEntity
import com.anoncommunity.infra.rdb.repository.MemberJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MemberCommandPortAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberCommandPort {

    @Transactional
    override fun save(member: Member): Member {
        val entity = MemberEntity.fromModel(member)
        return memberJpaRepository.save(entity).toModel()
    }
}

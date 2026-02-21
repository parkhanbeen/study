package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.infra.rdb.entity.MemberProfileEntity
import com.anoncommunity.infra.rdb.repository.MemberProfileRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProfileCommandPortAdapter(
    private val repository: MemberProfileRepository,
) : ProfileCommandPort {

    @Transactional
    override fun save(profile: MemberProfile): MemberProfile =
        repository.save(MemberProfileEntity.fromModel(profile)).toModel()

    @Transactional
    override fun delete(id: ProfileId) = repository.deleteById(id.value)
}

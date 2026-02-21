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

    @Transactional
    override fun incrementFollowerCount(profileId: ProfileId): Int =
        repository.incrementFollowerCount(profileId.value)

    @Transactional
    override fun decrementFollowerCount(profileId: ProfileId): Int =
        repository.decrementFollowerCount(profileId.value)

    @Transactional
    override fun incrementFollowingCount(profileId: ProfileId): Int =
        repository.incrementFollowingCount(profileId.value)

    @Transactional
    override fun decrementFollowingCount(profileId: ProfileId): Int =
        repository.decrementFollowingCount(profileId.value)

    @Transactional
    override fun incrementPostCount(profileId: ProfileId): Int =
        repository.incrementPostCount(profileId.value)

    @Transactional
    override fun decrementPostCount(profileId: ProfileId): Int =
        repository.decrementPostCount(profileId.value)
}

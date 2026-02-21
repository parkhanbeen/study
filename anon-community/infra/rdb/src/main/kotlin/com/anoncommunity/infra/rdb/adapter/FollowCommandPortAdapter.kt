package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.ProfileFollow
import com.anoncommunity.domain.member.port.FollowCommandPort
import com.anoncommunity.infra.rdb.entity.ProfileFollowEntity
import com.anoncommunity.infra.rdb.repository.ProfileFollowRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FollowCommandPortAdapter(
    private val repository: ProfileFollowRepository,
) : FollowCommandPort {

    @Transactional
    override fun save(follow: ProfileFollow): ProfileFollow =
        repository.save(ProfileFollowEntity.fromModel(follow)).toModel()

    @Transactional
    override fun delete(followerProfileId: ProfileId, followingProfileId: ProfileId) =
        repository.deleteByFollowerProfileIdAndFollowingProfileId(
            followerProfileId.value, followingProfileId.value,
        )
}

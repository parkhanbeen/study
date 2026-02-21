package com.anoncommunity.domain.member.exception

import com.anoncommunity.contract.model.ProfileId

class ProfileNotFoundException(
    val profileId: ProfileId,
) : RuntimeException("Profile not found: ${profileId.value}")

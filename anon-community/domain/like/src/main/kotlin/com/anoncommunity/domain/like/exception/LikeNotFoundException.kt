package com.anoncommunity.domain.like.exception

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId

class LikeNotFoundException(
    val postId: PostId,
    val profileId: ProfileId,
) : RuntimeException("Like not found for post ${postId.value} by profile ${profileId.value}")

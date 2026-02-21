package com.anoncommunity.domain.like.exception

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId

class PostAlreadyLikedException(
    val postId: PostId,
    val profileId: ProfileId,
) : RuntimeException("Profile ${profileId.value} already liked post ${postId.value}")

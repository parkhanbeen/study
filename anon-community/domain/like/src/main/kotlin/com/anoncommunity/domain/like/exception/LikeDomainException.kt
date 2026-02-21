package com.anoncommunity.domain.like.exception

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId

class PostAlreadyLikedException(
    val postId: PostId,
    val memberId: MemberId,
) : RuntimeException("Member ${memberId.value} already liked post ${postId.value}")

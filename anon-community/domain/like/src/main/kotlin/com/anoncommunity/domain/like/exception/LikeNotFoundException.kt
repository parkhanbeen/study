package com.anoncommunity.domain.like.exception

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId

class LikeNotFoundException(
    val postId: PostId,
    val memberId: MemberId,
) : RuntimeException("Like not found for post ${postId.value} by member ${memberId.value}")

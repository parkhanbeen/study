package com.anoncommunity.domain.post

import com.anoncommunity.contract.model.PostId

data class PostStats(
    val postId: PostId,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
)

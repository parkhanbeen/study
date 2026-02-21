package com.anoncommunity.domain.post.port

import com.anoncommunity.contract.model.PostId

interface PostStatsCommandPort {
    fun incrementLikeCount(postId: PostId): Int
    fun decrementLikeCount(postId: PostId): Int
    fun incrementCommentCount(postId: PostId): Int
    fun decrementCommentCount(postId: PostId): Int
}

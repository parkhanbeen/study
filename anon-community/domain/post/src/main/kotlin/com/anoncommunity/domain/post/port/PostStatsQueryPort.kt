package com.anoncommunity.domain.post.port

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.PostStats

interface PostStatsQueryPort {
    fun findByPostId(postId: PostId): PostStats?
    fun findByPostIds(postIds: List<PostId>): Map<PostId, PostStats>
}

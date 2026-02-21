package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.port.PostStatsCommandPort
import com.anoncommunity.infra.rdb.repository.PostStatsJpaRepository
import org.springframework.stereotype.Component

@Component
class PostStatsCommandPortAdapter(
    private val postStatsJpaRepository: PostStatsJpaRepository,
) : PostStatsCommandPort {

    override fun incrementLikeCount(postId: PostId): Int =
        postStatsJpaRepository.incrementLikeCount(postId.value)

    override fun decrementLikeCount(postId: PostId): Int =
        postStatsJpaRepository.decrementLikeCount(postId.value)

    override fun incrementCommentCount(postId: PostId): Int =
        postStatsJpaRepository.incrementCommentCount(postId.value)

    override fun decrementCommentCount(postId: PostId): Int =
        postStatsJpaRepository.decrementCommentCount(postId.value)
}

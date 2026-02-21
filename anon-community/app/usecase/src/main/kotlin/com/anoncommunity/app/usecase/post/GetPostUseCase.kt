package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostStats
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostQueryPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetPostQuery(val postId: Long)
data class PostWithStats(val post: Post, val stats: PostStats?)

@Service
@Transactional(readOnly = true)
class GetPostUseCase(
    private val postQueryPort: PostQueryPort,
    private val postStatsQueryPort: PostStatsQueryPort,
) : QueryUseCase<GetPostQuery, PostWithStats> {

    override fun invoke(query: GetPostQuery): PostWithStats {
        val postId = PostId(query.postId)
        val post = postQueryPort.findById(postId)
            ?: throw PostNotFoundException(postId)

        if (post.status != PostStatus.PUBLISHED) {
            throw PostNotFoundException(postId)
        }

        val stats = postStatsQueryPort.findByPostId(postId)
        return PostWithStats(post, stats)
    }
}

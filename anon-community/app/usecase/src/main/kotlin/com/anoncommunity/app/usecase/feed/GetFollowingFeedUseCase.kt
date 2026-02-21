package com.anoncommunity.app.usecase.feed

import com.anoncommunity.app.usecase.post.PostWithStats
import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.member.port.FollowingIdsCache
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.post.port.PostFeedPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetFollowingFeedQuery(val activeProfileId: Long, val pageable: DomainPageable = DomainPageable())

@Service
class GetFollowingFeedUseCase(
    private val followQueryPort: FollowQueryPort,
    private val postFeedPort: PostFeedPort,
    private val postStatsQueryPort: PostStatsQueryPort,
    private val followingIdsCache: FollowingIdsCache,
) : QueryUseCase<GetFollowingFeedQuery, PageResult<PostWithStats>> {

    @Transactional(readOnly = true)
    override fun invoke(query: GetFollowingFeedQuery): PageResult<PostWithStats> {
        val profileId = ProfileId(query.activeProfileId)

        // 1. Redis cache lookup (TTL 10 min)
        val followingIdLongs = followingIdsCache.get(profileId) ?: run {
            // 2. Cache miss → DB query
            val ids = followQueryPort.findFollowingIds(profileId, limit = 1000)
                .map { it.value }
            followingIdsCache.set(profileId, ids)
            ids
        }

        if (followingIdLongs.isEmpty()) {
            return PageResult(
                content = emptyList(),
                page = query.pageable.page,
                size = query.pageable.size,
                totalElements = 0L,
                totalPages = 0,
            )
        }

        // 3. OpenSearch following feed (List<Long> to avoid circular dependency with domain/member)
        val feedResult = postFeedPort.getFollowingFeed(followingIdLongs, query.pageable)
        val postIds = feedResult.content.map { it.id }
        val statsMap = postStatsQueryPort.findByPostIds(postIds)

        return PageResult(
            content = feedResult.content.map { post ->
                PostWithStats(post = post, stats = statsMap[post.id])
            },
            page = feedResult.page,
            size = feedResult.size,
            totalElements = feedResult.totalElements,
            totalPages = feedResult.totalPages,
        )
    }
}

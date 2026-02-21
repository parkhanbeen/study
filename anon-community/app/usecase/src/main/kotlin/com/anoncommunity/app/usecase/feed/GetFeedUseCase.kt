package com.anoncommunity.app.usecase.feed

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.post.port.PostFeedPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import com.anoncommunity.app.usecase.post.PostWithStats
import org.springframework.stereotype.Service

data class GetFeedQuery(
    val category: String? = null,
    val pageable: DomainPageable = DomainPageable(),
)

@Service
class GetFeedUseCase(
    private val postFeedPort: PostFeedPort,
    private val postStatsQueryPort: PostStatsQueryPort,
) : QueryUseCase<GetFeedQuery, PageResult<PostWithStats>> {

    override fun invoke(query: GetFeedQuery): PageResult<PostWithStats> {
        val feedResult = postFeedPort.getFeed(query.category, query.pageable)
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

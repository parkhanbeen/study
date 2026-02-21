package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.model.SortDirection
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.like.port.LikeQueryPort
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetMyLikedPostsQuery(
    val memberId: Long,
    val page: Int = 0,
    val size: Int = 20,
)

@Service
@Transactional(readOnly = true)
class GetMyLikedPostsUseCase(
    private val likeQueryPort: LikeQueryPort,
    private val postQueryPort: PostQueryPort,
) : QueryUseCase<GetMyLikedPostsQuery, PageResult<Post>> {

    override fun invoke(query: GetMyLikedPostsQuery): PageResult<Post> {
        val pageable = DomainPageable(
            page = query.page,
            size = query.size,
            sortBy = "createdAt",
            sortDirection = SortDirection.DESC,
        )
        val postIdsPage = likeQueryPort.findPostIdsByProfileId(ProfileId(query.memberId), pageable)
        // TODO: N+1 — replace with PostQueryPort.findByIds(ids) when added to the port
        val posts = postIdsPage.content.mapNotNull { postQueryPort.findById(it) }
        return PageResult(
            content = posts,
            page = postIdsPage.page,
            size = postIdsPage.size,
            totalElements = postIdsPage.totalElements,
            totalPages = postIdsPage.totalPages,
        )
    }
}

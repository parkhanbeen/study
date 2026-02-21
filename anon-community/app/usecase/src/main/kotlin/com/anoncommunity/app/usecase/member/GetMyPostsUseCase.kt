package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.SortDirection
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetMyPostsQuery(
    val memberId: Long,
    val page: Int = 0,
    val size: Int = 20,
)

@Service
@Transactional(readOnly = true)
class GetMyPostsUseCase(
    private val postQueryPort: PostQueryPort,
) : QueryUseCase<GetMyPostsQuery, PageResult<Post>> {

    override fun invoke(query: GetMyPostsQuery): PageResult<Post> {
        val pageable = DomainPageable(
            page = query.page,
            size = query.size,
            sortBy = "createdAt",
            sortDirection = SortDirection.DESC,
        )
        return postQueryPort.findByAuthorId(MemberId(query.memberId), pageable)
    }
}

package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.usecase.QueryUseCase
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.port.CommentQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetCommentsQuery(
    val postId: Long,
    val pageable: DomainPageable = DomainPageable(),
)

@Service
@Transactional(readOnly = true)
class GetCommentsUseCase(
    private val commentQueryPort: CommentQueryPort,
) : QueryUseCase<GetCommentsQuery, PageResult<Comment>> {

    override fun invoke(query: GetCommentsQuery): PageResult<Comment> =
        commentQueryPort.findByPostId(PostId(query.postId), query.pageable)
}

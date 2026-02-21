package com.anoncommunity.domain.comment.port

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.comment.Comment

interface CommentQueryPort {
    fun findById(id: CommentId): Comment?
    fun findByPostId(postId: PostId, pageable: DomainPageable): PageResult<Comment>
}

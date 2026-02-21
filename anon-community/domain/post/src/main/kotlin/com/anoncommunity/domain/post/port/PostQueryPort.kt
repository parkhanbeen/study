package com.anoncommunity.domain.post.port

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post

interface PostQueryPort {
    fun findById(id: PostId): Post?
    fun findByAuthorId(authorId: MemberId, pageable: DomainPageable): PageResult<Post>
}

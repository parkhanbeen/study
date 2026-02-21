package com.anoncommunity.domain.like.port

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.like.PostLike

interface LikeQueryPort {
    fun findByPostIdAndMemberId(postId: PostId, memberId: MemberId): PostLike?
    fun countByPostId(postId: PostId): Long
    fun existsByPostIdAndMemberId(postId: PostId, memberId: MemberId): Boolean
    fun findPostIdsByMemberId(memberId: MemberId, pageable: DomainPageable): PageResult<PostId>
}

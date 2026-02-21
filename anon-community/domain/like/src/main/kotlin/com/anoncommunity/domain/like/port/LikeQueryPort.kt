package com.anoncommunity.domain.like.port

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.PostLike

interface LikeQueryPort {
    fun findByPostIdAndProfileId(postId: PostId, profileId: ProfileId): PostLike?
    fun countByPostId(postId: PostId): Long
    fun existsByPostIdAndProfileId(postId: PostId, profileId: ProfileId): Boolean
    fun findPostIdsByProfileId(profileId: ProfileId, pageable: DomainPageable): PageResult<PostId>
}

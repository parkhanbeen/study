package com.anoncommunity.domain.like.port

import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.domain.like.PostLike

interface LikeCommandPort {
    fun save(postLike: PostLike): PostLike
    fun delete(id: PostLikeId)
}

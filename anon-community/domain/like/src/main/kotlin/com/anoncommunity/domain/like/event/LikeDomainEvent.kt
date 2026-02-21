package com.anoncommunity.domain.like.event

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId

sealed class LikeDomainEvent(
    override val aggregateId: String,
) : DomainEvent(aggregateId) {

    data class PostLiked(
        val postLikeId: PostLikeId,
        val postId: PostId,
        val profileId: ProfileId,
    ) : LikeDomainEvent(postLikeId.value.toString())

    data class PostUnliked(
        val postLikeId: PostLikeId,
        val postId: PostId,
        val profileId: ProfileId,
    ) : LikeDomainEvent(postLikeId.value.toString())
}

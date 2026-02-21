package com.anoncommunity.domain.like

import com.anoncommunity.contract.AggregateRoot
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.event.LikeDomainEvent
import java.time.Instant

class PostLike(
    val id: PostLikeId,
    val postId: PostId,
    val profileId: ProfileId,
    val createdAt: Instant,
) : AggregateRoot() {

    fun unlike() {
        recordDomainEvent(
            LikeDomainEvent.PostUnliked(
                postLikeId = id,
                postId = postId,
                profileId = profileId,
            )
        )
    }

    companion object {
        fun like(
            id: PostLikeId,
            postId: PostId,
            profileId: ProfileId,
            postAuthorProfileId: ProfileId,
        ): PostLike {
            require(profileId != postAuthorProfileId) { "Cannot like your own post" }

            // Note: Domain events are NOT recorded here because the aggregate ID (PostLikeId)
            // is not yet assigned (auto-increment). The UseCase publishes PostLiked event
            // after save, when the real ID is available.
            return PostLike(
                id = id,
                postId = postId,
                profileId = profileId,
                createdAt = Instant.now(),
            )
        }
    }
}

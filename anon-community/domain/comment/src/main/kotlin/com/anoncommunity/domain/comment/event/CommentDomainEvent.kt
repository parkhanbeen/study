package com.anoncommunity.domain.comment.event

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId

sealed class CommentDomainEvent(
    override val aggregateId: String,
) : DomainEvent(aggregateId) {

    data class CommentCreated(
        val commentId: CommentId,
        val postId: PostId,
        val authorProfileId: ProfileId,
        val parentCommentId: CommentId?,
    ) : CommentDomainEvent(commentId.value.toString())

    data class CommentDeleted(
        val commentId: CommentId,
        val postId: PostId,
        val authorProfileId: ProfileId,
    ) : CommentDomainEvent(commentId.value.toString())
}

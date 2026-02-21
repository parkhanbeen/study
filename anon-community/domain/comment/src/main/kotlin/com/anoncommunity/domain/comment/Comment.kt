package com.anoncommunity.domain.comment

import com.anoncommunity.contract.AggregateRoot
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.comment.event.CommentDomainEvent
import java.time.Instant

class Comment(
    val id: CommentId,
    val postId: PostId,
    val authorId: MemberId,
    content: CommentContent,
    val parentCommentId: CommentId?,
    val depth: Int,
    status: CommentStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) : AggregateRoot() {

    var content: CommentContent = content
        private set

    var status: CommentStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun update(newContent: CommentContent) {
        check(status == CommentStatus.ACTIVE) { "Cannot update a deleted comment" }
        this.content = newContent
        this.updatedAt = Instant.now()
    }

    fun softDelete() {
        check(status != CommentStatus.DELETED) { "Comment is already deleted" }

        this.status = CommentStatus.DELETED
        this.updatedAt = Instant.now()
        recordDomainEvent(
            CommentDomainEvent.CommentDeleted(
                commentId = id,
                postId = postId,
                authorId = authorId,
            )
        )
    }

    companion object {
        const val MAX_DEPTH = 1

        fun create(
            id: CommentId,
            postId: PostId,
            authorId: MemberId,
            content: CommentContent,
            parentCommentId: CommentId? = null,
            depth: Int = 0,
        ): Comment {
            require(depth <= MAX_DEPTH) { "Comment depth must not exceed $MAX_DEPTH" }
            if (depth > 0) {
                requireNotNull(parentCommentId) { "Reply comment must have a parent comment" }
            }

            // Note: Domain events are NOT recorded here because the aggregate ID (CommentId)
            // is not yet assigned (auto-increment). The UseCase publishes CommentCreated event
            // after save, when the real ID is available.
            val now = Instant.now()
            return Comment(
                id = id,
                postId = postId,
                authorId = authorId,
                content = content,
                parentCommentId = parentCommentId,
                depth = depth,
                status = CommentStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

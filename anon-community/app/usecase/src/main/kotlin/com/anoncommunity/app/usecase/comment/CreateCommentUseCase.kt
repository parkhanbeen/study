package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.event.CommentDomainEvent
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateCommentCommand(
    val postId: Long,
    val authorProfileId: Long,
    val content: String,
    val parentCommentId: Long? = null,
)

@Service
class CreateCommentUseCase(
    private val commentCommandPort: CommentCommandPort,
    private val commentQueryPort: CommentQueryPort,
    private val postQueryPort: PostQueryPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<CreateCommentCommand, Comment> {

    @Transactional
    override fun invoke(command: CreateCommentCommand): Comment {
        val profileId = ProfileId(command.authorProfileId)

        val postId = PostId(command.postId)
        val post = postQueryPort.findById(postId)
            ?: throw PostNotFoundException(postId)
        check(post.status == PostStatus.PUBLISHED) { "Cannot comment on a post with status: ${post.status}" }

        val depth = if (command.parentCommentId != null) {
            val parentCommentId = CommentId(command.parentCommentId)
            val parent = commentQueryPort.findById(parentCommentId)
                ?: throw CommentNotFoundException(parentCommentId)
            check(parent.status == CommentStatus.ACTIVE) { "Cannot reply to a deleted comment" }
            parent.depth + 1
        } else {
            0
        }

        val comment = Comment.create(
            id = CommentId.UNSAVED,
            postId = PostId(command.postId),
            authorProfileId = profileId,
            content = CommentContent(command.content),
            parentCommentId = command.parentCommentId?.let { CommentId(it) },
            depth = depth,
        )

        val saved = commentCommandPort.save(comment)

        eventPublisher.publish(
            listOf(
                CommentDomainEvent.CommentCreated(
                    commentId = saved.id,
                    postId = saved.postId,
                    authorProfileId = saved.authorProfileId,
                    parentCommentId = saved.parentCommentId,
                )
            )
        )

        return saved
    }
}

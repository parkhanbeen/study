package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class DeleteCommentCommand(
    val commentId: Long,
    val requesterProfileId: Long,
)

@Service
class DeleteCommentUseCase(
    private val commentQueryPort: CommentQueryPort,
    private val commentCommandPort: CommentCommandPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<DeleteCommentCommand, Unit> {

    @Transactional
    override fun invoke(command: DeleteCommentCommand) {
        val commentId = CommentId(command.commentId)
        val comment = commentQueryPort.findById(commentId)
            ?: throw CommentNotFoundException(commentId)

        if (comment.status == CommentStatus.DELETED) {
            throw CommentNotFoundException(commentId)
        }

        // 타인 댓글 삭제 방지: authorProfileId 노출 방지를 위해 CommentNotFoundException 사용 (UpdateCommentUseCase와 일관성)
        if (comment.authorProfileId != ProfileId(command.requesterProfileId)) {
            throw CommentNotFoundException(commentId)
        }

        comment.softDelete()

        val events = comment.pullDomainEvents()
        commentCommandPort.save(comment)

        eventPublisher.publish(events)
    }
}

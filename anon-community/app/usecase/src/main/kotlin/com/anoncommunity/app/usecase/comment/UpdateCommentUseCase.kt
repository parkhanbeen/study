package com.anoncommunity.app.usecase.comment

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.domain.comment.port.CommentQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdateCommentCommand(
    val commentId: Long,
    val requesterProfileId: Long,
    val content: String,
)

@Service
class UpdateCommentUseCase(
    private val commentQueryPort: CommentQueryPort,
    private val commentCommandPort: CommentCommandPort,
) : CommandUseCase<UpdateCommentCommand, Comment> {

    @Transactional
    override fun invoke(command: UpdateCommentCommand): Comment {
        val commentId = CommentId(command.commentId)
        val comment = commentQueryPort.findById(commentId)
            ?: throw CommentNotFoundException(commentId)

        // 타인 댓글 수정 방지: authorProfileId 노출 방지를 위해 CommentNotFoundException 사용
        if (comment.authorProfileId != ProfileId(command.requesterProfileId)) {
            throw CommentNotFoundException(commentId)
        }

        // 삭제된 댓글 수정 방지: DeleteCommentUseCase와 동일한 예외로 일관성 유지
        if (comment.status != CommentStatus.ACTIVE) {
            throw CommentNotFoundException(commentId)
        }

        comment.update(CommentContent(command.content))
        return commentCommandPort.save(comment)
    }
}

package com.anoncommunity.domain.comment.exception

import com.anoncommunity.contract.model.CommentId

class CommentNotFoundException(
    val commentId: CommentId,
) : RuntimeException("Comment not found: ${commentId.value}")

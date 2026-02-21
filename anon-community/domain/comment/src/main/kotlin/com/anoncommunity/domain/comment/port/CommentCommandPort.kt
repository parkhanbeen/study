package com.anoncommunity.domain.comment.port

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.domain.comment.Comment

interface CommentCommandPort {
    fun save(comment: Comment): Comment

    /** Hard-delete. Prefer [Comment.softDelete] + [save] to preserve domain events (CDC). Use only for admin purge. */
    fun delete(id: CommentId)
}

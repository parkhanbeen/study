package com.anoncommunity.domain.comment.port

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.domain.comment.Comment

interface CommentCommandPort {
    fun save(comment: Comment): Comment
    fun delete(id: CommentId)
}

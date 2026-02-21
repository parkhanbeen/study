package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.infra.rdb.entity.CommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CommentJpaRepository : JpaRepository<CommentEntity, Long> {

    /**
     * Returns comments in thread order: parent first, then its replies chronologically.
     * COALESCE(parent_comment_id, id) groups replies under their parent.
     */
    @Query(
        "SELECT c FROM CommentEntity c WHERE c.postId = :postId AND c.status <> :status " +
            "ORDER BY COALESCE(c.parentCommentId, c.id) ASC, c.depth ASC, c.createdAt ASC",
        countQuery = "SELECT COUNT(c) FROM CommentEntity c WHERE c.postId = :postId AND c.status <> :status",
    )
    fun findByPostIdAndStatusNot(postId: Long, status: CommentStatus, pageable: Pageable): Page<CommentEntity>
}

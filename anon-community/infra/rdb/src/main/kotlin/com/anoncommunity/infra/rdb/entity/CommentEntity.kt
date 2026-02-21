package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentContent
import com.anoncommunity.domain.comment.CommentStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "comments")
class CommentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "post_id", nullable = false)
    val postId: Long = 0L,

    @Column(name = "author_profile_id", nullable = false)
    val authorProfileId: Long = 0L,

    @Column(nullable = false, length = 1000)
    val content: String = "",

    @Column(name = "parent_comment_id")
    val parentCommentId: Long? = null,

    @Column(nullable = false)
    val depth: Int = 0,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: CommentStatus = CommentStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }

    fun toModel(): Comment = Comment(
        id = CommentId(id),
        postId = PostId(postId),
        authorProfileId = ProfileId(authorProfileId),
        content = CommentContent(content),
        parentCommentId = parentCommentId?.let { CommentId(it) },
        depth = depth,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(comment: Comment): CommentEntity = CommentEntity(
            id = comment.id.value,
            postId = comment.postId.value,
            authorProfileId = comment.authorProfileId.value,
            content = comment.content.value,
            parentCommentId = comment.parentCommentId?.value,
            depth = comment.depth,
            status = comment.status,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
        )
    }
}

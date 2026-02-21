package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.PostStats
import jakarta.persistence.*

@Entity
@Table(name = "post_stats")
class PostStatsEntity(
    @Id
    @Column(name = "post_id")
    val postId: Long = 0L,

    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,

    @Column(name = "comment_count", nullable = false)
    var commentCount: Int = 0,

    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0,
) {
    fun toModel(): PostStats = PostStats(
        postId = PostId(postId),
        likeCount = likeCount,
        commentCount = commentCount,
        viewCount = viewCount,
    )
}

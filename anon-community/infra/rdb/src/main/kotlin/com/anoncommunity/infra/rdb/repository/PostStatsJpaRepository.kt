package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.PostStatsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

/**
 * Atomic counter operations for post statistics.
 *
 * All increment/decrement operations use `SET count = count + 1` pattern, which is
 * safe from lost updates under concurrent access. However, hot posts may experience
 * row-level lock contention when multiple consumers (like + comment) update the same row
 * simultaneously. This is an acceptable trade-off for correctness.
 *
 * If contention becomes a bottleneck (observable via InnoDB row lock wait metrics in Datadog),
 * consider: (1) splitting like_count and comment_count into separate tables, or
 * (2) buffering increments in Redis and flushing periodically.
 *
 * clearAutomatically = true ensures the persistence context is cleared after each update,
 * preventing stale reads within the same transaction.
 */
interface PostStatsJpaRepository : JpaRepository<PostStatsEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostStatsEntity s SET s.likeCount = s.likeCount + 1 WHERE s.postId = :postId")
    fun incrementLikeCount(postId: Long): Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostStatsEntity s SET s.likeCount = GREATEST(s.likeCount - 1, 0) WHERE s.postId = :postId")
    fun decrementLikeCount(postId: Long): Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostStatsEntity s SET s.commentCount = s.commentCount + 1 WHERE s.postId = :postId")
    fun incrementCommentCount(postId: Long): Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostStatsEntity s SET s.commentCount = GREATEST(s.commentCount - 1, 0) WHERE s.postId = :postId")
    fun decrementCommentCount(postId: Long): Int
}

package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.PostLikeEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostLikeJpaRepository : JpaRepository<PostLikeEntity, Long> {
    fun findByPostIdAndMemberId(postId: Long, memberId: Long): PostLikeEntity?
    fun countByPostId(postId: Long): Long
    fun existsByPostIdAndMemberId(postId: Long, memberId: Long): Boolean

    @Query("SELECT pl.postId FROM PostLikeEntity pl WHERE pl.memberId = :memberId ORDER BY pl.createdAt DESC")
    fun findPostIdsByMemberId(memberId: Long, pageable: Pageable): Page<Long>
}

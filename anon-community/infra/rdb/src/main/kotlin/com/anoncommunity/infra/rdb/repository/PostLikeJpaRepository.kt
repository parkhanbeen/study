package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.PostLikeEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostLikeJpaRepository : JpaRepository<PostLikeEntity, Long> {
    fun findByPostIdAndProfileId(postId: Long, profileId: Long): PostLikeEntity?
    fun countByPostId(postId: Long): Long
    fun existsByPostIdAndProfileId(postId: Long, profileId: Long): Boolean

    @Query("SELECT pl.postId FROM PostLikeEntity pl WHERE pl.profileId = :profileId ORDER BY pl.createdAt DESC")
    fun findPostIdsByProfileId(profileId: Long, pageable: Pageable): Page<Long>
}

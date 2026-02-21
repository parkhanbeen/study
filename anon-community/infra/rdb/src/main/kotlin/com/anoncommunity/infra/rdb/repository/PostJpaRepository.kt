package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.infra.rdb.entity.PostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostJpaRepository : JpaRepository<PostEntity, Long> {
    fun findByAuthorProfileIdAndStatusNot(authorProfileId: Long, status: PostStatus, pageable: Pageable): Page<PostEntity>
}

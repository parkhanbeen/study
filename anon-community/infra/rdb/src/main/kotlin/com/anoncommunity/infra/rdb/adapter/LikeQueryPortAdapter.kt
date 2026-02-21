package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.like.PostLike
import com.anoncommunity.domain.like.port.LikeQueryPort
import com.anoncommunity.infra.rdb.repository.PostLikeJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class LikeQueryPortAdapter(
    private val postLikeJpaRepository: PostLikeJpaRepository,
) : LikeQueryPort {

    override fun findByPostIdAndMemberId(postId: PostId, memberId: MemberId): PostLike? =
        postLikeJpaRepository.findByPostIdAndMemberId(postId.value, memberId.value)?.toModel()

    override fun countByPostId(postId: PostId): Long =
        postLikeJpaRepository.countByPostId(postId.value)

    override fun existsByPostIdAndMemberId(postId: PostId, memberId: MemberId): Boolean =
        postLikeJpaRepository.existsByPostIdAndMemberId(postId.value, memberId.value)

    override fun findPostIdsByMemberId(memberId: MemberId, pageable: DomainPageable): PageResult<PostId> {
        val jpaPageable = PageRequest.of(pageable.page, pageable.size)
        val page = postLikeJpaRepository.findPostIdsByMemberId(memberId.value, jpaPageable)
        return PageResult(
            content = page.content.map { PostId(it) },
            page = pageable.page,
            size = pageable.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}

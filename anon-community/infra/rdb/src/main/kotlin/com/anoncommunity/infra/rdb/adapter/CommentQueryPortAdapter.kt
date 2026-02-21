package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.*
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.CommentStatus
import com.anoncommunity.domain.comment.port.CommentQueryPort
import com.anoncommunity.infra.rdb.repository.CommentJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class CommentQueryPortAdapter(
    private val commentJpaRepository: CommentJpaRepository,
) : CommentQueryPort {

    override fun findById(id: CommentId): Comment? =
        commentJpaRepository.findByIdOrNull(id.value)?.toModel()

    override fun findByPostId(postId: PostId, pageable: DomainPageable): PageResult<Comment> {
        // Use unsorted PageRequest — sort is defined in JPQL (thread order)
        val pageRequest = PageRequest.of(pageable.page, pageable.size)
        val page = commentJpaRepository.findByPostIdAndStatusNot(postId.value, CommentStatus.DELETED, pageRequest)
        return PageResult(
            content = page.content.map { it.toModel() },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}

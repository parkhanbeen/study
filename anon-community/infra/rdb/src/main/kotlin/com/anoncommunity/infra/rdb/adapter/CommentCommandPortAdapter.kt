package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.comment.port.CommentCommandPort
import com.anoncommunity.infra.rdb.entity.CommentEntity
import com.anoncommunity.infra.rdb.repository.CommentJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommentCommandPortAdapter(
    private val commentJpaRepository: CommentJpaRepository,
) : CommentCommandPort {

    @Transactional
    override fun save(comment: Comment): Comment {
        val entity = CommentEntity.fromModel(comment)
        return commentJpaRepository.save(entity).toModel()
    }

    @Transactional
    override fun delete(id: CommentId) {
        commentJpaRepository.deleteById(id.value)
    }
}

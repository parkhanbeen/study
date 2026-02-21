package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.domain.like.PostLike
import com.anoncommunity.domain.like.exception.PostAlreadyLikedException
import com.anoncommunity.domain.like.port.LikeCommandPort
import com.anoncommunity.infra.rdb.entity.PostLikeEntity
import com.anoncommunity.infra.rdb.repository.PostLikeJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeCommandPortAdapter(
    private val postLikeJpaRepository: PostLikeJpaRepository,
) : LikeCommandPort {

    @Transactional
    override fun save(postLike: PostLike): PostLike {
        try {
            val entity = PostLikeEntity.fromModel(postLike)
            return postLikeJpaRepository.save(entity).toModel()
        } catch (e: DataIntegrityViolationException) {
            throw PostAlreadyLikedException(postLike.postId, postLike.profileId)
        }
    }

    @Transactional
    override fun delete(id: PostLikeId) {
        postLikeJpaRepository.deleteById(id.value)
    }
}

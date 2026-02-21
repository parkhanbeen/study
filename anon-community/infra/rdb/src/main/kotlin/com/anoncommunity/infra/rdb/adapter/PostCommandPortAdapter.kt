package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.port.PostCommandPort
import com.anoncommunity.infra.rdb.entity.PostEntity
import com.anoncommunity.infra.rdb.entity.PostStatsEntity
import com.anoncommunity.infra.rdb.repository.PostJpaRepository
import com.anoncommunity.infra.rdb.repository.PostStatsJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PostCommandPortAdapter(
    private val postJpaRepository: PostJpaRepository,
    private val postStatsJpaRepository: PostStatsJpaRepository,
) : PostCommandPort {

    @Transactional
    override fun save(post: Post): Post {
        val entity = PostEntity.fromModel(post)
        val saved = postJpaRepository.save(entity)

        if (post.id == PostId.UNSAVED) {
            postStatsJpaRepository.save(PostStatsEntity(postId = saved.id))
        }

        return saved.toModel()
    }

    @Transactional
    override fun delete(id: PostId) {
        postJpaRepository.deleteById(id.value)
    }
}

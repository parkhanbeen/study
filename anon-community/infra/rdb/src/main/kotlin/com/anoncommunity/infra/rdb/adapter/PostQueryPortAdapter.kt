package com.anoncommunity.infra.rdb.adapter

import com.anoncommunity.contract.model.*
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostStats
import com.anoncommunity.domain.post.port.PostQueryPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import com.anoncommunity.infra.rdb.repository.PostJpaRepository
import com.anoncommunity.infra.rdb.repository.PostStatsJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PostQueryPortAdapter(
    private val postJpaRepository: PostJpaRepository,
    private val postStatsJpaRepository: PostStatsJpaRepository,
) : PostQueryPort, PostStatsQueryPort {

    override fun findById(id: PostId): Post? =
        postJpaRepository.findByIdOrNull(id.value)?.toModel()

    // NOTE: PostQueryPort.findByAuthorId still uses MemberId. This will be updated
    // in a later task when PostQueryPort is refactored to use ProfileId.
    // The value is forwarded as-is; semantically this will be a profileId once the port is updated.
    override fun findByAuthorId(authorId: MemberId, pageable: DomainPageable): PageResult<Post> {
        val pageRequest = pageable.toPageRequest()
        val page = postJpaRepository.findByAuthorProfileIdAndStatusNot(authorId.value, PostStatus.DELETED, pageRequest)
        return PageResult(
            content = page.content.map { it.toModel() },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }

    override fun findByPostId(postId: PostId): PostStats? =
        postStatsJpaRepository.findByIdOrNull(postId.value)?.toModel()

    override fun findByPostIds(postIds: List<PostId>): Map<PostId, PostStats> {
        if (postIds.isEmpty()) return emptyMap()
        return postStatsJpaRepository.findAllById(postIds.map { it.value })
            .associate { PostId(it.postId) to it.toModel() }
    }
}

internal fun DomainPageable.toPageRequest(): PageRequest {
    val direction = when (sortDirection) {
        SortDirection.ASC -> Sort.Direction.ASC
        SortDirection.DESC -> Sort.Direction.DESC
    }
    return PageRequest.of(page, size, Sort.by(direction, sortBy))
}

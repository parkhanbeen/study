package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostCategory
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.Tag
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostCommandPort
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdatePostCommand(
    val postId: Long,
    val requesterId: Long,
    val title: String? = null,
    val content: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
)

@Service
class UpdatePostUseCase(
    private val postQueryPort: PostQueryPort,
    private val postCommandPort: PostCommandPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<UpdatePostCommand, Post> {

    @Transactional
    override fun invoke(command: UpdatePostCommand): Post {
        val postId = PostId(command.postId)
        val post = postQueryPort.findById(postId)
            ?: throw PostNotFoundException(postId)

        if (post.status != PostStatus.PUBLISHED) {
            throw PostNotFoundException(postId)
        }

        if (post.authorProfileId != ProfileId(command.requesterId)) {
            throw AccessDeniedException("Only the author can edit this post")
        }

        post.edit(
            title = command.title?.let { PostTitle(it) },
            content = command.content?.let { PostContent(it) },
            category = command.category?.let { PostCategory.valueOf(it) },
            tags = command.tags?.map { Tag(it) },
        )

        val events = post.pullDomainEvents()
        val saved = postCommandPort.save(post)

        eventPublisher.publish(events)

        return saved
    }
}

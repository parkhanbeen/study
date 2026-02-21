package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostCategory
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.Tag
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.port.PostCommandPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreatePostCommand(
    val authorProfileId: Long,
    val title: String,
    val content: String,
    val category: String,
    val tags: List<String> = emptyList(),
)

@Service
class CreatePostUseCase(
    private val postCommandPort: PostCommandPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<CreatePostCommand, Post> {

    @Transactional
    override fun invoke(command: CreatePostCommand): Post {
        val profileId = ProfileId(command.authorProfileId)
        val category = PostCategory.valueOf(command.category)

        val post = Post.create(
            id = PostId.UNSAVED,
            authorProfileId = profileId,
            title = PostTitle(command.title),
            content = PostContent(command.content),
            category = category,
            tags = command.tags.map { Tag(it) },
        )

        val saved = postCommandPort.save(post)

        eventPublisher.publish(
            listOf(
                PostDomainEvent.PostCreated(
                    postId = saved.id,
                    authorProfileId = saved.authorProfileId,
                    title = saved.title.value,
                    tags = saved.tags.map(Tag::value),
                )
            )
        )

        return saved
    }
}

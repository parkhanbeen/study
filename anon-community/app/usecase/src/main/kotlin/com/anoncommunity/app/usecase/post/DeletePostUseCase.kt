package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostCommandPort
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class DeletePostCommand(
    val postId: Long,
    val requesterProfileId: Long,
)

@Service
class DeletePostUseCase(
    private val postQueryPort: PostQueryPort,
    private val postCommandPort: PostCommandPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<DeletePostCommand, Unit> {

    @Transactional
    override fun invoke(command: DeletePostCommand) {
        val postId = PostId(command.postId)
        val post = postQueryPort.findById(postId)
            ?: throw PostNotFoundException(postId)

        if (post.authorProfileId != ProfileId(command.requesterProfileId)) {
            throw AccessDeniedException("Only the author can delete this post")
        }

        post.softDelete()

        val events = post.pullDomainEvents()
        postCommandPort.save(post)

        eventPublisher.publish(events)
    }
}

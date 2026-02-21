package com.anoncommunity.app.usecase.like

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.like.exception.LikeNotFoundException
import com.anoncommunity.domain.like.port.LikeCommandPort
import com.anoncommunity.domain.like.port.LikeQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UnlikePostCommand(
    val postId: Long,
    val profileId: Long,
)

@Service
class UnlikePostUseCase(
    private val likeQueryPort: LikeQueryPort,
    private val likeCommandPort: LikeCommandPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<UnlikePostCommand, Unit> {

    @Transactional
    override fun invoke(command: UnlikePostCommand) {
        val postId = PostId(command.postId)
        val profileId = ProfileId(command.profileId)
        val postLike = likeQueryPort.findByPostIdAndProfileId(postId, profileId)
            ?: throw LikeNotFoundException(postId, profileId)

        postLike.unlike()

        val events = postLike.pullDomainEvents()
        likeCommandPort.delete(postLike.id)

        eventPublisher.publish(events)
    }
}

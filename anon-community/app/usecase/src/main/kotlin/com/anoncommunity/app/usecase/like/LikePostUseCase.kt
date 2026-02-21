package com.anoncommunity.app.usecase.like

import com.anoncommunity.contract.event.DomainEventPublisher
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.like.PostLike
import com.anoncommunity.domain.like.event.LikeDomainEvent
import com.anoncommunity.domain.like.exception.PostAlreadyLikedException
import com.anoncommunity.domain.like.port.LikeCommandPort
import com.anoncommunity.domain.like.port.LikeQueryPort
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class LikePostCommand(
    val postId: Long,
    val profileId: Long,
)

@Service
class LikePostUseCase(
    private val likeCommandPort: LikeCommandPort,
    private val likeQueryPort: LikeQueryPort,
    private val postQueryPort: PostQueryPort,
    private val eventPublisher: DomainEventPublisher,
) : CommandUseCase<LikePostCommand, PostLike> {

    @Transactional
    override fun invoke(command: LikePostCommand): PostLike {
        val postId = PostId(command.postId)
        val profileId = ProfileId(command.profileId)

        val post = postQueryPort.findById(postId)
            ?: throw PostNotFoundException(postId)
        check(post.status == PostStatus.PUBLISHED) { "Cannot like a post with status: ${post.status}" }

        if (likeQueryPort.existsByPostIdAndProfileId(postId, profileId)) {
            throw PostAlreadyLikedException(postId, profileId)
        }

        val postLike = PostLike.like(
            id = PostLikeId.UNSAVED,
            postId = postId,
            profileId = profileId,
            postAuthorProfileId = post.authorProfileId,
        )

        val saved = likeCommandPort.save(postLike)

        eventPublisher.publish(
            listOf(
                LikeDomainEvent.PostLiked(
                    postLikeId = saved.id,
                    postId = saved.postId,
                    profileId = saved.profileId,
                )
            )
        )

        return saved
    }
}

package com.anoncommunity.domain.post.event

import com.anoncommunity.contract.event.DomainEvent
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.Tag

sealed class PostDomainEvent(
    override val aggregateId: String,
) : DomainEvent(aggregateId) {

    data class PostCreated(
        val postId: PostId,
        val authorProfileId: ProfileId,
        val title: String,
        val tags: List<String>,
    ) : PostDomainEvent(postId.value.toString())

    data class PostEdited(
        val postId: PostId,
        val changedFields: Set<PostField>,
    ) : PostDomainEvent(postId.value.toString())

    data class PostDeleted(
        val postId: PostId,
        val authorProfileId: ProfileId,
    ) : PostDomainEvent(postId.value.toString())
}

enum class PostField {
    TITLE,
    CONTENT,
    CATEGORY,
    TAGS,
}

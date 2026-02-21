package com.anoncommunity.domain.post

import com.anoncommunity.contract.AggregateRoot
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.event.PostDomainEvent
import com.anoncommunity.domain.post.event.PostField
import java.time.Instant

class Post(
    val id: PostId,
    val authorId: MemberId,
    title: PostTitle,
    content: PostContent,
    category: PostCategory,
    tags: List<Tag>,
    status: PostStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) : AggregateRoot() {

    var title: PostTitle = title
        private set

    var content: PostContent = content
        private set

    var category: PostCategory = category
        private set

    var tags: List<Tag> = tags.toList()
        private set

    var status: PostStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun edit(
        title: PostTitle? = null,
        content: PostContent? = null,
        category: PostCategory? = null,
        tags: List<Tag>? = null,
    ) {
        check(status == PostStatus.PUBLISHED) { "Can only edit a published post, current status: $status" }

        val changedFields = mutableSetOf<PostField>()

        title?.let {
            if (it != this.title) {
                this.title = it
                changedFields.add(PostField.TITLE)
            }
        }

        content?.let {
            if (it != this.content) {
                this.content = it
                changedFields.add(PostField.CONTENT)
            }
        }

        category?.let {
            if (it != this.category) {
                this.category = it
                changedFields.add(PostField.CATEGORY)
            }
        }

        tags?.let {
            if (it != this.tags) {
                this.tags = it.toList()
                changedFields.add(PostField.TAGS)
            }
        }

        if (changedFields.isNotEmpty()) {
            this.updatedAt = Instant.now()
            recordDomainEvent(
                PostDomainEvent.PostEdited(
                    postId = id,
                    changedFields = changedFields,
                )
            )
        }
    }

    fun softDelete() {
        check(status != PostStatus.DELETED) { "Post is already deleted" }

        this.status = PostStatus.DELETED
        this.updatedAt = Instant.now()
        recordDomainEvent(
            PostDomainEvent.PostDeleted(
                postId = id,
                authorId = authorId,
            )
        )
    }

    fun hide() {
        check(status == PostStatus.PUBLISHED) { "Can only hide published posts" }
        this.status = PostStatus.HIDDEN
        this.updatedAt = Instant.now()
    }

    fun markForReview() {
        check(status == PostStatus.PUBLISHED || status == PostStatus.HIDDEN) {
            "Can only review published or hidden posts"
        }
        this.status = PostStatus.UNDER_REVIEW
        this.updatedAt = Instant.now()
    }

    fun remove() {
        check(status != PostStatus.DELETED && status != PostStatus.REMOVED) {
            "Post is already removed or deleted"
        }
        this.status = PostStatus.REMOVED
        this.updatedAt = Instant.now()
    }

    fun restore() {
        check(status == PostStatus.HIDDEN || status == PostStatus.UNDER_REVIEW) {
            "Can only restore hidden or under-review posts"
        }
        this.status = PostStatus.PUBLISHED
        this.updatedAt = Instant.now()
    }

    companion object {
        fun create(
            id: PostId,
            authorId: MemberId,
            title: PostTitle,
            content: PostContent,
            category: PostCategory = PostCategory.DAILY,
            tags: List<Tag> = emptyList(),
        ): Post {
            val now = Instant.now()
            // Note: Domain events are NOT recorded here because the aggregate ID (PostId)
            // is not yet assigned (auto-increment). The UseCase publishes PostCreated event
            // after save, when the real ID is available.
            return Post(
                id = id,
                authorId = authorId,
                title = title,
                content = content,
                category = category,
                tags = tags,
                status = PostStatus.PUBLISHED,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

enum class PostStatus {
    PUBLISHED,
    HIDDEN,
    UNDER_REVIEW,
    REMOVED,
    DELETED,
}

package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.*
import com.anoncommunity.infra.rdb.converter.TagsJsonConverter
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "posts")
class PostEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "author_profile_id", nullable = false)
    val authorProfileId: Long = 0L,

    @Column(nullable = false, length = 200)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var category: PostCategory = PostCategory.DAILY,

    @Column(columnDefinition = "JSON")
    @Convert(converter = TagsJsonConverter::class)
    var tags: List<String> = emptyList(),

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: PostStatus = PostStatus.PUBLISHED,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }

    fun toModel(): Post = Post(
        id = PostId(id),
        authorProfileId = ProfileId(authorProfileId),
        title = PostTitle(title),
        content = PostContent(content),
        category = category,
        tags = tags.map { Tag(it) },
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(post: Post): PostEntity = PostEntity(
            id = post.id.value,
            authorProfileId = post.authorProfileId.value,
            title = post.title.value,
            content = post.content.value,
            category = post.category,
            tags = post.tags.map { it.value },
            status = post.status,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
        )
    }
}

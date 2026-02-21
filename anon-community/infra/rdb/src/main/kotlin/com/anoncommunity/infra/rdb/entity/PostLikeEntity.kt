package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.PostLikeId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.PostLike
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "profile_id"])],
)
class PostLikeEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "post_id", nullable = false)
    val postId: Long = 0L,

    @Column(name = "profile_id", nullable = false)
    val profileId: Long = 0L,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) {
    fun toModel(): PostLike = PostLike(
        id = PostLikeId(id),
        postId = PostId(postId),
        profileId = ProfileId(profileId),
        createdAt = createdAt,
    )

    companion object {
        fun fromModel(postLike: PostLike): PostLikeEntity = PostLikeEntity(
            id = postLike.id.value,
            postId = postLike.postId.value,
            profileId = postLike.profileId.value,
            createdAt = postLike.createdAt,
        )
    }
}

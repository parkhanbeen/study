package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.ProfileFollowId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.ProfileFollow
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "profile_follows",
    uniqueConstraints = [UniqueConstraint(columnNames = ["follower_profile_id", "following_profile_id"])],
)
class ProfileFollowEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "follower_profile_id", nullable = false)
    val followerProfileId: Long = 0L,

    @Column(name = "following_profile_id", nullable = false)
    val followingProfileId: Long = 0L,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) {
    fun toModel(): ProfileFollow = ProfileFollow.fromDb(
        id = ProfileFollowId(id),
        followerProfileId = ProfileId(followerProfileId),
        followingProfileId = ProfileId(followingProfileId),
        createdAt = createdAt,
    )

    companion object {
        fun fromModel(follow: ProfileFollow): ProfileFollowEntity = ProfileFollowEntity(
            id = follow.id.value,
            followerProfileId = follow.followerProfileId.value,
            followingProfileId = follow.followingProfileId.value,
            createdAt = follow.createdAt,
        )
    }
}

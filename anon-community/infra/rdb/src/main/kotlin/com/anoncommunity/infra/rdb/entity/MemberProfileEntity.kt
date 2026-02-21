package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "member_profiles")
class MemberProfileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0L,

    @Column(nullable = false, unique = true, length = 20)
    var nickname: String = "",

    @Column(length = 200)
    var bio: String? = null,

    @Column(name = "post_count", nullable = false)
    var postCount: Int = 0,

    @Column(name = "follower_count", nullable = false)
    var followerCount: Int = 0,

    @Column(name = "following_count", nullable = false)
    var followingCount: Int = 0,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }

    fun toModel(): MemberProfile = MemberProfile(
        id = ProfileId(id),
        memberId = MemberId(memberId),
        nickname = MemberNickname(nickname),
        bio = Bio(bio ?: ""),
        postCount = postCount,
        followerCount = followerCount,
        followingCount = followingCount,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(profile: MemberProfile): MemberProfileEntity = MemberProfileEntity(
            id = profile.id.value,
            memberId = profile.memberId.value,
            nickname = profile.nickname.value,
            bio = profile.bio.value.ifBlank { null },
            postCount = profile.postCount,
            followerCount = profile.followerCount,
            followingCount = profile.followingCount,
            isDefault = profile.isDefault,
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt,
        )
    }
}

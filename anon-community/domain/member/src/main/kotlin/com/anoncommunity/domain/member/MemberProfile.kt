package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import java.time.Instant

class MemberProfile(
    val id: ProfileId,
    val memberId: MemberId,
    nickname: MemberNickname,
    bio: Bio,
    postCount: Int = 0,
    followerCount: Int = 0,
    followingCount: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) {
    var nickname: MemberNickname = nickname
        private set

    var bio: Bio = bio
        private set

    var postCount: Int = postCount
        private set

    var followerCount: Int = followerCount
        private set

    var followingCount: Int = followingCount
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun updateProfile(nickname: MemberNickname, bio: Bio): MemberProfile =
        MemberProfile(
            id = this.id,
            memberId = this.memberId,
            nickname = nickname,
            bio = bio,
            postCount = this.postCount,
            followerCount = this.followerCount,
            followingCount = this.followingCount,
            isDefault = this.isDefault,
            createdAt = this.createdAt,
            updatedAt = Instant.now(),
        )

    fun incrementPostCount() {
        postCount = maxOf(0, postCount + 1)
        updatedAt = Instant.now()
    }

    fun decrementPostCount() {
        postCount = maxOf(0, postCount - 1)
        updatedAt = Instant.now()
    }

    fun incrementFollowerCount() {
        followerCount = maxOf(0, followerCount + 1)
        updatedAt = Instant.now()
    }

    fun decrementFollowerCount() {
        followerCount = maxOf(0, followerCount - 1)
        updatedAt = Instant.now()
    }

    fun incrementFollowingCount() {
        followingCount = maxOf(0, followingCount + 1)
        updatedAt = Instant.now()
    }

    fun decrementFollowingCount() {
        followingCount = maxOf(0, followingCount - 1)
        updatedAt = Instant.now()
    }

    fun requireDeletable() {
        check(!isDefault) { "Default profile cannot be deleted" }
    }

    companion object {
        fun create(
            id: ProfileId,
            memberId: MemberId,
            nickname: MemberNickname,
            bio: Bio,
            isDefault: Boolean = false,
        ): MemberProfile {
            val now = Instant.now()
            return MemberProfile(
                id = id,
                memberId = memberId,
                nickname = nickname,
                bio = bio,
                postCount = 0,
                followerCount = 0,
                followingCount = 0,
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

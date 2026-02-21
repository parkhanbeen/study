package com.anoncommunity.domain.member

import com.anoncommunity.contract.AggregateRoot
import com.anoncommunity.contract.model.MemberId
import java.time.Instant

class Member(
    val id: MemberId,
    nickname: MemberNickname,
    bio: Bio,
    passwordHash: String?,
    postCount: Int,
    status: MemberStatus,
    val createdAt: Instant,
    updatedAt: Instant,
    preferredLanguage: PreferredLanguage?,
) : AggregateRoot() {

    var nickname: MemberNickname = nickname
        private set

    var bio: Bio = bio
        private set

    var passwordHash: String? = passwordHash
        private set

    var postCount: Int = postCount
        private set

    var status: MemberStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    var preferredLanguage: PreferredLanguage? = preferredLanguage
        private set

    fun updatePreferredLanguage(language: PreferredLanguage) {
        this.preferredLanguage = language
        this.updatedAt = Instant.now()
    }

    fun incrementPostCount() {
        postCount++
        updatedAt = Instant.now()
    }

    fun decrementPostCount() {
        check(postCount > 0) { "Post count cannot be negative" }
        postCount--
        updatedAt = Instant.now()
    }

    fun updateProfile(
        nickname: MemberNickname? = null,
        bio: Bio? = null,
    ) {
        var changed = false
        nickname?.let { this.nickname = it; changed = true }
        bio?.let { this.bio = it; changed = true }
        if (changed) updatedAt = Instant.now()
    }

    fun setPassword(hashedPassword: String) {
        this.passwordHash = hashedPassword
        this.updatedAt = Instant.now()
    }

    fun suspend() {
        check(status == MemberStatus.ACTIVE) { "Can only suspend active members" }
        this.status = MemberStatus.SUSPENDED
        this.updatedAt = Instant.now()
    }

    fun ban() {
        check(status != MemberStatus.BANNED) { "Member is already banned" }
        this.status = MemberStatus.BANNED
        this.updatedAt = Instant.now()
    }

    fun activate() {
        check(status == MemberStatus.SUSPENDED) { "Can only activate suspended members" }
        this.status = MemberStatus.ACTIVE
        this.updatedAt = Instant.now()
    }

    val isActive: Boolean get() = status == MemberStatus.ACTIVE

    companion object {
        fun create(
            id: MemberId,
            nickname: MemberNickname,
            bio: Bio = Bio(""),
            passwordHash: String? = null,
        ): Member {
            val now = Instant.now()
            return Member(
                id = id,
                nickname = nickname,
                bio = bio,
                passwordHash = passwordHash,
                postCount = 0,
                status = MemberStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
                preferredLanguage = null,
            )
        }
    }
}

enum class MemberStatus {
    ACTIVE,
    SUSPENDED,
    BANNED,
}

package com.anoncommunity.domain.member

import com.anoncommunity.contract.AggregateRoot
import com.anoncommunity.contract.model.MemberId
import java.time.Instant

class Member(
    val id: MemberId,
    passwordHash: String?,
    status: MemberStatus,
    val createdAt: Instant,
    updatedAt: Instant,
    preferredLanguage: PreferredLanguage?,
) : AggregateRoot() {

    var passwordHash: String? = passwordHash
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
            passwordHash: String? = null,
        ): Member {
            val now = Instant.now()
            return Member(
                id = id,
                passwordHash = passwordHash,
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

package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberStatus
import com.anoncommunity.domain.member.PreferredLanguage
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "members")
class MemberEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "password_hash", length = 72)
    var passwordHash: String? = null,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Column(name = "preferred_language", length = 5)
    var preferredLanguage: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }

    fun toModel(): Member = Member(
        id = MemberId(id),
        passwordHash = passwordHash,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        preferredLanguage = preferredLanguage?.let { PreferredLanguage(it) },
    )

    companion object {
        fun fromModel(member: Member): MemberEntity = MemberEntity(
            id = member.id.value,
            passwordHash = member.passwordHash,
            status = member.status,
            preferredLanguage = member.preferredLanguage?.value,
            createdAt = member.createdAt,
            updatedAt = member.updatedAt,
        )
    }
}

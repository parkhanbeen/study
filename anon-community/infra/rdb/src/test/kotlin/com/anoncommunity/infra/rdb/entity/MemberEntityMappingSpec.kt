package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberStatus
import com.anoncommunity.domain.member.PreferredLanguage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class MemberEntityMappingSpec : DescribeSpec({

    val now = Instant.parse("2025-01-15T10:30:00Z")

    fun createMemberEntity(
        id: Long = 1L,
        passwordHash: String? = "hashed_pw",
        status: MemberStatus = MemberStatus.ACTIVE,
        preferredLanguage: String? = "en",
    ) = MemberEntity(
        id = id,
        passwordHash = passwordHash,
        status = status,
        preferredLanguage = preferredLanguage,
        createdAt = now,
        updatedAt = now,
    )

    describe("MemberEntity.fromModel") {

        it("should map all fields correctly") {
            val member = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed_pw",
            ).also { it.updatePreferredLanguage(PreferredLanguage("en")) }

            val entity = MemberEntity.fromModel(member)

            entity.id shouldBe 1L
            entity.passwordHash shouldBe "hashed_pw"
            entity.status shouldBe MemberStatus.ACTIVE
            entity.preferredLanguage shouldBe "en"
        }

        it("should map null preferredLanguage to null") {
            val member = Member.create(
                id = MemberId(2L),
                passwordHash = "hash",
            )

            val entity = MemberEntity.fromModel(member)

            entity.preferredLanguage shouldBe null
        }
    }

    describe("MemberEntity.toModel") {

        it("should reconstruct Member correctly") {
            val entity = createMemberEntity()

            val member = entity.toModel()

            member.id shouldBe MemberId(1L)
            member.passwordHash shouldBe "hashed_pw"
            member.status shouldBe MemberStatus.ACTIVE
            member.preferredLanguage shouldBe PreferredLanguage("en")
            member.createdAt shouldBe now
            member.updatedAt shouldBe now
        }

        it("should map null preferredLanguage to null") {
            val entity = createMemberEntity(preferredLanguage = null)

            val member = entity.toModel()

            member.preferredLanguage shouldBe null
        }

        it("should map preferredLanguage string to PreferredLanguage") {
            val entity = createMemberEntity(preferredLanguage = "ko")

            val member = entity.toModel()

            member.preferredLanguage shouldBe PreferredLanguage("ko")
        }
    }

    describe("roundtrip") {

        it("should preserve all fields through fromModel -> toModel") {
            val original = Member.create(
                id = MemberId(1L),
                passwordHash = "hashed_pw",
            ).also { it.updatePreferredLanguage(PreferredLanguage("en")) }

            val roundtripped = MemberEntity.fromModel(original).toModel()

            roundtripped.id shouldBe original.id
            roundtripped.passwordHash shouldBe original.passwordHash
            roundtripped.status shouldBe original.status
            roundtripped.preferredLanguage shouldBe original.preferredLanguage
        }
    }
})

package com.anoncommunity.infra.rdb.entity

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberStatus
import com.anoncommunity.domain.member.PreferredLanguage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class MemberEntityMappingSpec : DescribeSpec({

    val now = Instant.parse("2025-01-15T10:30:00Z")

    fun createMember(
        id: Long = 1L,
        nickname: String = "testuser",
        bio: String = "hello world",
        passwordHash: String? = "hashed_pw",
        postCount: Int = 5,
        status: MemberStatus = MemberStatus.ACTIVE,
        preferredLanguage: PreferredLanguage? = PreferredLanguage("en"),
    ) = Member(
        id = MemberId(id),
        nickname = MemberNickname(nickname),
        bio = Bio(bio),
        passwordHash = passwordHash,
        postCount = postCount,
        status = status,
        createdAt = now,
        updatedAt = now,
        preferredLanguage = preferredLanguage,
    )

    describe("MemberEntity.fromModel") {

        it("should map all fields correctly") {
            val member = createMember()
            val entity = MemberEntity.fromModel(member)

            entity.id shouldBe 1L
            entity.nickname shouldBe "testuser"
            entity.bio shouldBe "hello world"
            entity.passwordHash shouldBe "hashed_pw"
            entity.postCount shouldBe 5
            entity.status shouldBe MemberStatus.ACTIVE
            entity.preferredLanguage shouldBe "en"
            entity.createdAt shouldBe now
            entity.updatedAt shouldBe now
        }

        it("should convert blank bio to null") {
            val member = createMember(bio = "")
            val entity = MemberEntity.fromModel(member)

            entity.bio shouldBe null
        }

        it("should map preferredLanguage value") {
            val member = createMember(preferredLanguage = PreferredLanguage("en"))
            val entity = MemberEntity.fromModel(member)

            entity.preferredLanguage shouldBe "en"
        }

        it("should map null preferredLanguage to null") {
            val member = createMember(preferredLanguage = null)
            val entity = MemberEntity.fromModel(member)

            entity.preferredLanguage shouldBe null
        }
    }

    describe("MemberEntity.toModel") {

        it("should reconstruct Member correctly") {
            val entity = MemberEntity(
                id = 1L,
                nickname = "testuser",
                bio = "hello world",
                passwordHash = "hashed_pw",
                postCount = 5,
                status = MemberStatus.ACTIVE,
                preferredLanguage = "en",
                createdAt = now,
                updatedAt = now,
            )

            val member = entity.toModel()

            member.id shouldBe MemberId(1L)
            member.nickname shouldBe MemberNickname("testuser")
            member.bio shouldBe Bio("hello world")
            member.passwordHash shouldBe "hashed_pw"
            member.postCount shouldBe 5
            member.status shouldBe MemberStatus.ACTIVE
            member.preferredLanguage shouldBe PreferredLanguage("en")
            member.createdAt shouldBe now
            member.updatedAt shouldBe now
        }

        it("should default null bio to empty string") {
            val entity = MemberEntity(
                id = 2L,
                nickname = "nullbio",
                bio = null,
                createdAt = now,
                updatedAt = now,
            )

            val member = entity.toModel()

            member.bio shouldBe Bio("")
        }

        it("should map null preferredLanguage to null") {
            val entity = MemberEntity(
                id = 3L,
                nickname = "nolang",
                preferredLanguage = null,
                createdAt = now,
                updatedAt = now,
            )

            val member = entity.toModel()

            member.preferredLanguage shouldBe null
        }

        it("should map preferredLanguage string to PreferredLanguage") {
            val entity = MemberEntity(
                id = 4L,
                nickname = "kolang",
                preferredLanguage = "ko",
                createdAt = now,
                updatedAt = now,
            )

            val member = entity.toModel()

            member.preferredLanguage shouldBe PreferredLanguage("ko")
        }
    }

    describe("roundtrip") {

        it("should preserve all fields through fromModel -> toModel") {
            val original = createMember()
            val roundtripped = MemberEntity.fromModel(original).toModel()

            roundtripped.id shouldBe original.id
            roundtripped.nickname shouldBe original.nickname
            roundtripped.bio shouldBe original.bio
            roundtripped.passwordHash shouldBe original.passwordHash
            roundtripped.postCount shouldBe original.postCount
            roundtripped.status shouldBe original.status
            roundtripped.preferredLanguage shouldBe original.preferredLanguage
            roundtripped.createdAt shouldBe original.createdAt
            roundtripped.updatedAt shouldBe original.updatedAt
        }
    }
})

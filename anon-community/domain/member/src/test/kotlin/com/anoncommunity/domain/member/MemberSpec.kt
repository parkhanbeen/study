package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.MemberId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe

class MemberSpec : DescribeSpec({

    fun createActiveMember(postCount: Int = 0) = Member.create(
        id = MemberId(1L),
        nickname = MemberNickname("testuser"),
        bio = Bio("hello"),
    ).also { repeat(postCount) { _ -> it.incrementPostCount() } }

    describe("Member.create") {
        it("should create member with ACTIVE status, zero postCount, null preferredLanguage") {
            val member = createActiveMember()
            member.status shouldBe MemberStatus.ACTIVE
            member.postCount shouldBe 0
            member.preferredLanguage shouldBe null
            member.isActive shouldBe true
        }
    }

    describe("Member.suspend") {
        it("should transition ACTIVE -> SUSPENDED") {
            val member = createActiveMember()
            member.suspend()
            member.status shouldBe MemberStatus.SUSPENDED
            member.isActive shouldBe false
        }

        it("should throw when suspending non-ACTIVE member") {
            val member = createActiveMember()
            member.suspend()
            shouldThrow<IllegalStateException> { member.suspend() }
        }

        it("should throw when suspending BANNED member") {
            val member = createActiveMember()
            member.ban()
            shouldThrow<IllegalStateException> { member.suspend() }
        }
    }

    describe("Member.ban") {
        it("should transition ACTIVE -> BANNED") {
            val member = createActiveMember()
            member.ban()
            member.status shouldBe MemberStatus.BANNED
        }

        it("should transition SUSPENDED -> BANNED") {
            val member = createActiveMember()
            member.suspend()
            member.ban()
            member.status shouldBe MemberStatus.BANNED
        }

        it("should throw when already BANNED") {
            val member = createActiveMember()
            member.ban()
            shouldThrow<IllegalStateException> { member.ban() }
        }
    }

    describe("Member.activate") {
        it("should transition SUSPENDED -> ACTIVE") {
            val member = createActiveMember()
            member.suspend()
            member.activate()
            member.status shouldBe MemberStatus.ACTIVE
        }

        it("should throw when activating ACTIVE member") {
            val member = createActiveMember()
            shouldThrow<IllegalStateException> { member.activate() }
        }

        it("should throw when activating BANNED member") {
            val member = createActiveMember()
            member.ban()
            shouldThrow<IllegalStateException> { member.activate() }
        }
    }

    describe("Member.incrementPostCount") {
        it("should increment by 1 and update updatedAt") {
            val member = createActiveMember()
            val before = member.updatedAt
            member.incrementPostCount()
            member.postCount shouldBe 1
            member.updatedAt shouldBeGreaterThanOrEqualTo before
        }
    }

    describe("Member.decrementPostCount") {
        it("should decrement by 1") {
            val member = createActiveMember(postCount = 2)
            member.decrementPostCount()
            member.postCount shouldBe 1
        }

        it("should throw when postCount is 0") {
            val member = createActiveMember()
            shouldThrow<IllegalStateException> { member.decrementPostCount() }
        }
    }

    describe("Member.updateProfile") {
        it("should update nickname only") {
            val member = createActiveMember()
            member.updateProfile(nickname = MemberNickname("newnick"))
            member.nickname shouldBe MemberNickname("newnick")
        }

        it("should update bio only") {
            val member = createActiveMember()
            member.updateProfile(bio = Bio("new bio"))
            member.bio shouldBe Bio("new bio")
        }

        it("should not change updatedAt when no args provided") {
            val member = createActiveMember()
            val before = member.updatedAt
            member.updateProfile()
            member.updatedAt shouldBe before
        }
    }

    describe("Member.updatePreferredLanguage") {
        it("should set language and update updatedAt") {
            val member = createActiveMember()
            val before = member.updatedAt
            member.updatePreferredLanguage(PreferredLanguage("en"))
            member.preferredLanguage shouldBe PreferredLanguage("en")
            member.updatedAt shouldBeGreaterThanOrEqualTo before
        }
    }
})

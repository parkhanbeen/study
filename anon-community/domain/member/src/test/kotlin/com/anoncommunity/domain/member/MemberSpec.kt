package com.anoncommunity.domain.member

import com.anoncommunity.contract.model.MemberId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe

class MemberSpec : DescribeSpec({

    fun createActiveMember() = Member.create(
        id = MemberId(1L),
    )

    describe("Member.create") {
        it("should create member with ACTIVE status, null preferredLanguage") {
            val member = createActiveMember()
            member.status shouldBe MemberStatus.ACTIVE
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

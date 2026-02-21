package com.anoncommunity.domain.member

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MemberValuesSpec : DescribeSpec({

    describe("MemberNickname") {
        it("should accept 2-char nickname") {
            MemberNickname("ab").value shouldBe "ab"
        }

        it("should accept 20-char nickname") {
            MemberNickname("a".repeat(20)).value shouldBe "a".repeat(20)
        }

        it("should accept Korean characters") {
            shouldNotThrow<IllegalArgumentException> { MemberNickname("홍길동") }
        }

        it("should accept Japanese characters") {
            shouldNotThrow<IllegalArgumentException> { MemberNickname("太郎") }
        }

        it("should accept dots, underscores, hyphens") {
            shouldNotThrow<IllegalArgumentException> { MemberNickname("user.name_test-1") }
        }

        it("should reject single character") {
            shouldThrow<IllegalArgumentException> { MemberNickname("a") }
        }

        it("should reject 21 characters") {
            shouldThrow<IllegalArgumentException> { MemberNickname("a".repeat(21)) }
        }

        it("should reject spaces") {
            shouldThrow<IllegalArgumentException> { MemberNickname("hello world") }
        }

        it("should reject special characters") {
            shouldThrow<IllegalArgumentException> { MemberNickname("user@name") }
        }
    }

    describe("Bio") {
        it("should accept empty string") { Bio("").value shouldBe "" }
        it("should accept exactly 200 characters") { shouldNotThrow<IllegalArgumentException> { Bio("a".repeat(200)) } }
        it("should reject 201 characters") { shouldThrow<IllegalArgumentException> { Bio("a".repeat(201)) } }
    }

    describe("PreferredLanguage") {
        listOf("ko", "en", "ja", "zh").forEach { lang ->
            it("should accept '$lang'") { PreferredLanguage(lang).value shouldBe lang }
        }
        listOf("fr", "de", "", "KO").forEach { invalid ->
            it("should reject '$invalid'") { shouldThrow<IllegalArgumentException> { PreferredLanguage(invalid) } }
        }
    }
})

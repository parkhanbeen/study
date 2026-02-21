package com.anoncommunity.domain.post

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PostValueObjectSpec : DescribeSpec({

    describe("PostTitle") {
        it("should create a valid title") {
            val title = PostTitle("Valid Title")
            title.value shouldBe "Valid Title"
        }

        it("should reject blank title") {
            val exception = shouldThrow<IllegalArgumentException> {
                PostTitle("")
            }
            exception.message shouldContain "must not be blank"
        }

        it("should reject whitespace-only title") {
            shouldThrow<IllegalArgumentException> {
                PostTitle("   ")
            }
        }

        it("should reject title exceeding max length") {
            val exception = shouldThrow<IllegalArgumentException> {
                PostTitle("a".repeat(PostTitle.MAX_LENGTH + 1))
            }
            exception.message shouldContain "${PostTitle.MAX_LENGTH}"
        }

        it("should accept title at max length") {
            val title = PostTitle("a".repeat(PostTitle.MAX_LENGTH))
            title.value.length shouldBe PostTitle.MAX_LENGTH
        }
    }

    describe("PostContent") {
        it("should create valid content") {
            val content = PostContent("Valid content body")
            content.value shouldBe "Valid content body"
        }

        it("should reject blank content") {
            shouldThrow<IllegalArgumentException> {
                PostContent("")
            }
        }

        it("should reject content exceeding max length") {
            shouldThrow<IllegalArgumentException> {
                PostContent("a".repeat(PostContent.MAX_LENGTH + 1))
            }
        }
    }

    describe("Tag") {
        it("should create a valid tag") {
            val tag = Tag("kotlin")
            tag.value shouldBe "kotlin"
        }

        it("should reject blank tag") {
            shouldThrow<IllegalArgumentException> {
                Tag("")
            }
        }

        it("should reject whitespace-only tag") {
            shouldThrow<IllegalArgumentException> {
                Tag("   ")
            }
        }

        it("should reject tag with spaces") {
            val exception = shouldThrow<IllegalArgumentException> {
                Tag("spring boot")
            }
            exception.message shouldContain "spaces"
        }

        it("should reject tag exceeding max length") {
            shouldThrow<IllegalArgumentException> {
                Tag("a".repeat(Tag.MAX_LENGTH + 1))
            }
        }

        it("should accept tag at max length") {
            val tag = Tag("a".repeat(Tag.MAX_LENGTH))
            tag.value.length shouldBe Tag.MAX_LENGTH
        }
    }
})

package com.anoncommunity.app.consumer.cdc

import com.anoncommunity.infra.opensearch.adapter.PostIndexWriter
import com.anoncommunity.infra.opensearch.document.PostDocument
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*

class PostCdcConsumerSpec : DescribeSpec({

    val postIndexWriter = mockk<PostIndexWriter>(relaxed = true)
    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    val consumer = PostCdcConsumer(postIndexWriter, objectMapper)

    beforeEach {
        clearMocks(postIndexWriter)
    }

    describe("PostCdcConsumer") {

        context("tombstone message") {
            it("should skip null messages without error") {
                consumer.consume(null)

                verify(exactly = 0) { postIndexWriter.index(any()) }
                verify(exactly = 0) { postIndexWriter.delete(any()) }
            }
        }

        context("unwrapped (flattened) format — create/update") {
            it("should index a PostDocument from unwrapped CDC message") {
                val message = """
                    {
                        "id": 1,
                        "author_id": 100,
                        "title": "Hello World",
                        "content": "This is a test post",
                        "tags": "[\"kotlin\",\"spring\"]",
                        "status": "PUBLISHED",
                        "created_at": 1700000000000,
                        "updated_at": 1700000000000,
                        "__op": "c",
                        "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify(exactly = 1) { postIndexWriter.index(capture(captured)) }
                verify(exactly = 0) { postIndexWriter.delete(any()) }

                with(captured.captured) {
                    id shouldBe 1L
                    authorId shouldBe 100L
                    title shouldBe "Hello World"
                    content shouldBe "This is a test post"
                    tags shouldBe listOf("kotlin", "spring")
                    status shouldBe "PUBLISHED"
                }
            }

            it("should handle update operation (__op=u)") {
                val message = """
                    {
                        "id": 1,
                        "author_id": 100,
                        "title": "Updated Title",
                        "content": "Updated content",
                        "tags": null,
                        "status": "PUBLISHED",
                        "created_at": 1700000000000,
                        "updated_at": 1700001000000,
                        "__op": "u",
                        "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify(exactly = 1) { postIndexWriter.index(capture(captured)) }

                with(captured.captured) {
                    id shouldBe 1L
                    title shouldBe "Updated Title"
                    tags shouldBe emptyList()
                }
            }
        }

        context("unwrapped format — delete") {
            it("should delete when __op is d") {
                val message = """
                    {
                        "id": 42,
                        "author_id": 100,
                        "title": "To be deleted",
                        "content": "content",
                        "tags": null,
                        "status": "DELETED",
                        "created_at": 1700000000000,
                        "updated_at": 1700000000000,
                        "__op": "d",
                        "__deleted": "true"
                    }
                """.trimIndent()

                consumer.consume(message)

                verify(exactly = 0) { postIndexWriter.index(any()) }
                verify(exactly = 1) { postIndexWriter.delete(42L) }
            }

            it("should delete when __deleted is true even without __op=d") {
                val message = """
                    {
                        "id": 42,
                        "author_id": 100,
                        "title": "Deleted",
                        "content": "content",
                        "tags": null,
                        "status": "DELETED",
                        "created_at": 1700000000000,
                        "updated_at": 1700000000000,
                        "__op": "u",
                        "__deleted": "true"
                    }
                """.trimIndent()

                consumer.consume(message)

                verify(exactly = 0) { postIndexWriter.index(any()) }
                verify(exactly = 1) { postIndexWriter.delete(42L) }
            }
        }

        context("envelope format — create/update") {
            it("should index from envelope format create (op=c)") {
                val message = """
                    {
                        "before": null,
                        "after": {
                            "id": 5,
                            "author_id": 200,
                            "title": "Envelope Post",
                            "content": "Created via envelope",
                            "tags": "[\"java\"]",
                            "status": "PUBLISHED",
                            "created_at": 1700000000000,
                            "updated_at": 1700000000000
                        },
                        "op": "c"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify(exactly = 1) { postIndexWriter.index(capture(captured)) }

                with(captured.captured) {
                    id shouldBe 5L
                    authorId shouldBe 200L
                    title shouldBe "Envelope Post"
                    tags shouldBe listOf("java")
                }
            }

            it("should index from envelope format update (op=u)") {
                val message = """
                    {
                        "before": {
                            "id": 5,
                            "title": "Old Title"
                        },
                        "after": {
                            "id": 5,
                            "author_id": 200,
                            "title": "New Title",
                            "content": "Updated via envelope",
                            "tags": null,
                            "status": "PUBLISHED",
                            "created_at": 1700000000000,
                            "updated_at": 1700001000000
                        },
                        "op": "u"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify(exactly = 1) { postIndexWriter.index(capture(captured)) }

                captured.captured.title shouldBe "New Title"
            }
        }

        context("envelope format — delete") {
            it("should delete from envelope format (op=d)") {
                val message = """
                    {
                        "before": {
                            "id": 10,
                            "author_id": 200,
                            "title": "To Delete",
                            "content": "content",
                            "tags": null,
                            "status": "PUBLISHED",
                            "created_at": 1700000000000,
                            "updated_at": 1700000000000
                        },
                        "after": null,
                        "op": "d"
                    }
                """.trimIndent()

                consumer.consume(message)

                verify(exactly = 0) { postIndexWriter.index(any()) }
                verify(exactly = 1) { postIndexWriter.delete(10L) }
            }
        }

        context("tags parsing edge cases") {
            it("should handle empty tags array") {
                val message = """
                    {
                        "id": 1, "author_id": 1, "title": "t", "content": "c",
                        "tags": "[]",
                        "status": "PUBLISHED",
                        "created_at": 1700000000000, "updated_at": 1700000000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }
                captured.captured.tags shouldBe emptyList()
            }

            it("should handle null tags") {
                val message = """
                    {
                        "id": 1, "author_id": 1, "title": "t", "content": "c",
                        "tags": null,
                        "status": "PUBLISHED",
                        "created_at": 1700000000000, "updated_at": 1700000000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }
                captured.captured.tags shouldBe emptyList()
            }

            it("should handle tags with special characters") {
                val message = """
                    {
                        "id": 1, "author_id": 1, "title": "t", "content": "c",
                        "tags": "[\"C++\",\"C#\",\"한글태그\"]",
                        "status": "PUBLISHED",
                        "created_at": 1700000000000, "updated_at": 1700000000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }
                captured.captured.tags shouldBe listOf("C++", "C#", "한글태그")
            }
        }

        context("timestamp parsing error cases") {
            it("should throw when created_at is null") {
                val message = """
                    {
                        "id": 1, "author_id": 10, "title": "Test", "content": "Body",
                        "category": "DAILY", "status": "PUBLISHED",
                        "tags": "[]",
                        "created_at": null,
                        "updated_at": 1000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                val ex = shouldThrow<IllegalArgumentException> {
                    consumer.consume(message)
                }
                ex.message shouldBe "timestamp must not be null"
            }

            it("should throw when updated_at is null") {
                val message = """
                    {
                        "id": 1, "author_id": 10, "title": "Test", "content": "Body",
                        "category": "DAILY", "status": "PUBLISHED",
                        "tags": "[]",
                        "created_at": 1000000,
                        "updated_at": null,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                val ex = shouldThrow<IllegalArgumentException> {
                    consumer.consume(message)
                }
                ex.message shouldBe "timestamp must not be null"
            }

            it("should throw when tags JSON is invalid") {
                val message = """
                    {
                        "id": 1, "author_id": 10, "title": "Test", "content": "Body",
                        "category": "DAILY", "status": "PUBLISHED",
                        "tags": "INVALID_JSON",
                        "created_at": 1000000,
                        "updated_at": 1000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                val ex = shouldThrow<IllegalArgumentException> {
                    consumer.consume(message)
                }
                ex.message shouldContain "Cannot parse tags"
            }
        }

        context("timestamp parsing") {
            it("should parse epoch milliseconds") {
                val message = """
                    {
                        "id": 1, "author_id": 1, "title": "t", "content": "c",
                        "tags": null, "status": "PUBLISHED",
                        "created_at": 1700000000000, "updated_at": 1700001000000,
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }

                with(captured.captured) {
                    createdAt.toEpochMilli() shouldBe 1700000000000L
                    updatedAt.toEpochMilli() shouldBe 1700001000000L
                }
            }

            it("should parse ISO-8601 timestamp strings") {
                val message = """
                    {
                        "id": 1, "author_id": 1, "title": "t", "content": "c",
                        "tags": null, "status": "PUBLISHED",
                        "created_at": "2024-01-15T10:30:00Z",
                        "updated_at": "2024-01-15T11:00:00Z",
                        "__op": "c", "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }

                captured.captured.createdAt.toString() shouldBe "2024-01-15T10:30:00Z"
                captured.captured.updatedAt.toString() shouldBe "2024-01-15T11:00:00Z"
            }
        }

        context("chosung extraction in toPostDocument") {
            it("should populate titleChosung and contentChosung from Korean title/content") {
                val message = """
                    {
                        "id": 1,
                        "author_id": 100,
                        "title": "무신사 할인",
                        "content": "운동 추천합니다",
                        "tags": null,
                        "status": "PUBLISHED",
                        "created_at": 1700000000000,
                        "updated_at": 1700000000000,
                        "__op": "c",
                        "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }

                captured.captured.titleChosung shouldBe "ㅁㅅㅅㅎㅇ"
                captured.captured.contentChosung shouldBe "ㅇㄷㅊㅊㅎㄴㄷ"
            }

            it("should set empty titleChosung for non-Korean title") {
                val message = """
                    {
                        "id": 2,
                        "author_id": 100,
                        "title": "Hello World",
                        "content": "English content",
                        "tags": null,
                        "status": "PUBLISHED",
                        "created_at": 1700000000000,
                        "updated_at": 1700000000000,
                        "__op": "c",
                        "__deleted": "false"
                    }
                """.trimIndent()

                consumer.consume(message)

                val captured = slot<PostDocument>()
                verify { postIndexWriter.index(capture(captured)) }

                captured.captured.titleChosung shouldBe ""
                captured.captured.contentChosung shouldBe ""
            }
        }
    }
})

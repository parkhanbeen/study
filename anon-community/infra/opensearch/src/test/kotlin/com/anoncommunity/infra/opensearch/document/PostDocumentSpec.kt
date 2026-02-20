package com.anoncommunity.infra.opensearch.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class PostDocumentSpec : DescribeSpec({
    describe("PostDocument") {
        it("should include commentCount field with default value 0") {
            val doc = PostDocument(
                id = 1L,
                authorId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

            doc.commentCount shouldBe 0
        }

        it("should allow setting commentCount") {
            val doc = PostDocument(
                id = 1L,
                authorId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                commentCount = 7,
            )

            doc.commentCount shouldBe 7
        }
    }

    describe("JSON serialization") {
        it("should serialize and deserialize commentCount correctly") {
            val doc = PostDocument(
                id = 1L,
                authorId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                commentCount = 5,
            )

            val objectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            val json = objectMapper.writeValueAsString(doc)
            val deserialized = objectMapper.readValue(json, PostDocument::class.java)

            deserialized.commentCount shouldBe 5
        }

        it("should apply default commentCount=0 when field missing in JSON") {
            val json = """
                {
                    "id": 1,
                    "authorId": 100,
                    "title": "Test",
                    "content": "Content",
                    "category": "DAILY",
                    "tags": [],
                    "status": "PUBLISHED",
                    "createdAt": "2025-02-20T12:00:00Z",
                    "updatedAt": "2025-02-20T12:00:00Z"
                }
            """.trimIndent()

            val objectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())

            val doc = objectMapper.readValue(json, PostDocument::class.java)

            doc.commentCount shouldBe 0
        }
    }
})

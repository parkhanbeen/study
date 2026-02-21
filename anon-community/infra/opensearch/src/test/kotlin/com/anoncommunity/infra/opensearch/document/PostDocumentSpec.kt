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
                authorProfileId = 100L,
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
                authorProfileId = 100L,
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

        it("should include likeCount field with default value 0") {
            val doc = PostDocument(
                id = 1L,
                authorProfileId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

            doc.likeCount shouldBe 0
        }

        it("should include viewCount field with default value 0") {
            val doc = PostDocument(
                id = 1L,
                authorProfileId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )

            doc.viewCount shouldBe 0
        }

        it("should allow setting likeCount and viewCount") {
            val doc = PostDocument(
                id = 1L,
                authorProfileId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                likeCount = 42,
                viewCount = 100,
            )

            doc.likeCount shouldBe 42
            doc.viewCount shouldBe 100
        }
    }

        it("should include titleChosung field with default empty string") {
            val doc = PostDocument(
                id = 1L, authorProfileId = 100L, title = "Test", content = "Content",
                category = "DAILY", tags = emptyList(), status = "PUBLISHED",
                createdAt = Instant.now(), updatedAt = Instant.now(),
            )
            doc.titleChosung shouldBe ""
            doc.contentChosung shouldBe ""
        }

        it("should allow setting titleChosung and contentChosung") {
            val doc = PostDocument(
                id = 1L, authorProfileId = 100L, title = "무신사", content = "운동 추천",
                category = "DAILY", tags = emptyList(), status = "PUBLISHED",
                createdAt = Instant.now(), updatedAt = Instant.now(),
                titleChosung = "ㅁㅅㅅ",
                contentChosung = "ㅇㄷㅊㅊ",
            )
            doc.titleChosung shouldBe "ㅁㅅㅅ"
            doc.contentChosung shouldBe "ㅇㄷㅊㅊ"
        }

    describe("JSON serialization") {
        it("should serialize and deserialize commentCount correctly") {
            val doc = PostDocument(
                id = 1L,
                authorProfileId = 100L,
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
                    "authorProfileId": 100,
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

        it("should apply default likeCount=0 and viewCount=0 when fields missing in JSON (backward compat with posts-v2)") {
            // Documents indexed before posts-v3 will not have likeCount/viewCount in JSON
            // @JsonIgnoreProperties(ignoreUnknown = true) + Kotlin default values ensure safe deserialization
            val json = """
                {
                    "id": 1,
                    "authorProfileId": 100,
                    "title": "Test",
                    "content": "Content",
                    "category": "DAILY",
                    "tags": [],
                    "status": "PUBLISHED",
                    "createdAt": "2025-02-20T12:00:00Z",
                    "updatedAt": "2025-02-20T12:00:00Z",
                    "commentCount": 3
                }
            """.trimIndent()

            val objectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())

            val doc = objectMapper.readValue(json, PostDocument::class.java)

            doc.likeCount shouldBe 0
            doc.viewCount shouldBe 0
            doc.commentCount shouldBe 3  // existing field unaffected
        }

        it("should serialize and deserialize likeCount and viewCount correctly") {
            val doc = PostDocument(
                id = 1L,
                authorProfileId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                likeCount = 15,
                viewCount = 300,
            )

            val objectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            val json = objectMapper.writeValueAsString(doc)
            val deserialized = objectMapper.readValue(json, PostDocument::class.java)

            deserialized.likeCount shouldBe 15
            deserialized.viewCount shouldBe 300
        }
    }
})

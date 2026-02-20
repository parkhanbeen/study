package com.anoncommunity.infra.opensearch.adapter

import com.anoncommunity.infra.opensearch.document.PostDocument
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.http.HttpHost
import org.opensearch.action.get.GetRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import java.time.Duration
import java.time.Instant

class PostIndexWriterIntegrationSpec : DescribeSpec({

    val container = GenericContainer("opensearchproject/opensearch:2.11.0").apply {
        withExposedPorts(9200)
        withEnv("discovery.type", "single-node")
        withEnv("plugins.security.disabled", "true")
        withEnv("OPENSEARCH_INITIAL_ADMIN_PASSWORD", "Test!ng123")
        waitingFor(
            HttpWaitStrategy()
                .forPort(9200)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2))
        )
    }

    lateinit var client: RestHighLevelClient
    lateinit var postIndexWriter: PostIndexWriter

    beforeSpec {
        container.start()

        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        client = RestHighLevelClient(
            RestClient.builder(HttpHost("localhost", container.getMappedPort(9200), "http"))
        )
        postIndexWriter = PostIndexWriter(client, objectMapper)

        // Create the write index if not exists
        if (!client.indices().exists(GetIndexRequest(PostIndexWriter.INDEX_WRITE), RequestOptions.DEFAULT)) {
            val createRequest = CreateIndexRequest(PostIndexWriter.INDEX_WRITE)
            createRequest.mapping(
                """
                {
                    "properties": {
                        "id": { "type": "long" },
                        "authorId": { "type": "long" },
                        "title": { "type": "text" },
                        "content": { "type": "text" },
                        "tags": { "type": "keyword" },
                        "status": { "type": "keyword" },
                        "createdAt": { "type": "date" },
                        "updatedAt": { "type": "date" },
                        "commentCount": { "type": "integer" }
                    }
                }
                """.trimIndent(),
                XContentType.JSON,
            )
            client.indices().create(createRequest, RequestOptions.DEFAULT)
        }
    }

    afterSpec {
        client.close()
        container.stop()
    }

    describe("PostIndexWriter") {

        context("index") {
            it("should index a PostDocument and make it retrievable from OpenSearch") {
                val now = Instant.now()
                val document = PostDocument(
                    id = 1L,
                    authorId = 100L,
                    title = "Integration Test Post",
                    content = "This verifies that CDC indexing works end-to-end",
                    tags = listOf("kotlin", "opensearch"),
                    status = "PUBLISHED",
                    createdAt = now,
                    updatedAt = now,
                )

                postIndexWriter.index(document)
                Thread.sleep(1500) // OpenSearch near-real-time refresh

                val response = client.get(
                    GetRequest(PostIndexWriter.INDEX_WRITE, "1"),
                    RequestOptions.DEFAULT,
                )

                response.isExists shouldBe true

                val source = response.sourceAsMap
                source["id"] shouldBe 1
                source["authorId"] shouldBe 100
                source["title"] shouldBe "Integration Test Post"
                source["tags"] shouldBe listOf("kotlin", "opensearch")
                source["status"] shouldBe "PUBLISHED"
            }

            it("should upsert when indexing with the same ID") {
                val now = Instant.now()
                val original = PostDocument(
                    id = 2L, authorId = 100L, title = "Original",
                    content = "Original content", tags = listOf("v1"),
                    status = "PUBLISHED", createdAt = now, updatedAt = now,
                )
                postIndexWriter.index(original)

                val updated = original.copy(
                    title = "Updated Title",
                    tags = listOf("v2", "updated"),
                    updatedAt = Instant.now(),
                )
                postIndexWriter.index(updated)
                Thread.sleep(1500)

                val source = client.get(
                    GetRequest(PostIndexWriter.INDEX_WRITE, "2"),
                    RequestOptions.DEFAULT,
                ).sourceAsMap

                source["title"] shouldBe "Updated Title"
                source["tags"] shouldBe listOf("v2", "updated")
            }

            it("should index Korean content and empty tags correctly") {
                val now = Instant.now()
                val document = PostDocument(
                    id = 4L, authorId = 200L,
                    title = "한글 제목 테스트",
                    content = "한글 본문 내용입니다.",
                    tags = emptyList(),
                    status = "PUBLISHED",
                    createdAt = now, updatedAt = now,
                )

                postIndexWriter.index(document)
                Thread.sleep(1500)

                val source = client.get(
                    GetRequest(PostIndexWriter.INDEX_WRITE, "4"),
                    RequestOptions.DEFAULT,
                ).sourceAsMap

                source["title"] shouldBe "한글 제목 테스트"
                source["content"] shouldNotBe null
                source["tags"] shouldBe emptyList<String>()
            }
        }

        context("delete") {
            it("should remove a document from OpenSearch") {
                val now = Instant.now()
                val document = PostDocument(
                    id = 3L, authorId = 100L, title = "To Delete",
                    content = "Will be deleted", tags = emptyList(),
                    status = "PUBLISHED", createdAt = now, updatedAt = now,
                )
                postIndexWriter.index(document)
                Thread.sleep(1500)

                client.get(
                    GetRequest(PostIndexWriter.INDEX_WRITE, "3"),
                    RequestOptions.DEFAULT,
                ).isExists shouldBe true

                postIndexWriter.delete(3L)
                Thread.sleep(1500)

                client.get(
                    GetRequest(PostIndexWriter.INDEX_WRITE, "3"),
                    RequestOptions.DEFAULT,
                ).isExists shouldBe false
            }
        }
    }
})

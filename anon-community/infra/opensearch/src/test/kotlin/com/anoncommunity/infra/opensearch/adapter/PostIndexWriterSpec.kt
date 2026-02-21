package com.anoncommunity.infra.opensearch.adapter

import com.anoncommunity.infra.opensearch.document.PostDocument
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import java.io.IOException
import java.time.Instant

class PostIndexWriterSpec : DescribeSpec({

    val client = mockk<RestHighLevelClient>()
    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    val writer = PostIndexWriter(client, objectMapper)

    val document = PostDocument(
        id = 1L,
        authorId = 10L,
        title = "test",
        content = "body",
        category = "DAILY",
        tags = emptyList(),
        status = "PUBLISHED",
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    describe("index") {
        it("OpenSearch 연결 실패 시 IOException을 전파한다") {
            every { client.index(any(), RequestOptions.DEFAULT) } throws IOException("Connection refused")

            shouldThrow<IOException> {
                writer.index(document)
            }
        }
    }

    describe("delete") {
        it("OpenSearch 연결 실패 시 IOException을 전파한다") {
            every { client.delete(any(), RequestOptions.DEFAULT) } throws IOException("Connection refused")

            shouldThrow<IOException> {
                writer.delete(1L)
            }
        }
    }
})

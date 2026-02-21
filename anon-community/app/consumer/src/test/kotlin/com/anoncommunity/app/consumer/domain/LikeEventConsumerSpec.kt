package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.PostStats
import com.anoncommunity.domain.post.port.PostStatsCommandPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import com.anoncommunity.infra.opensearch.adapter.PostIndexWriter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*

class LikeEventConsumerSpec : DescribeSpec({

    val postStatsCommandPort = mockk<PostStatsCommandPort>()
    val postStatsQueryPort = mockk<PostStatsQueryPort>()
    val postIndexWriter = mockk<PostIndexWriter>(relaxed = true)
    val processedEventPort = mockk<ProcessedEventPort>()
    val objectMapper: ObjectMapper = jacksonObjectMapper()
    val consumer = LikeEventConsumer(postStatsCommandPort, postStatsQueryPort, postIndexWriter, processedEventPort, objectMapper)

    beforeEach {
        clearMocks(postStatsCommandPort, postStatsQueryPort, postIndexWriter, processedEventPort)
        every { postStatsQueryPort.findByPostId(any()) } returns null
    }

    describe("LikeEventConsumer") {

        context("PostLiked event") {

            it("should increment like count when event is newly processed") {
                every { processedEventPort.markProcessed("evt-1", "PostLiked") } returns true
                every { postStatsCommandPort.incrementLikeCount(PostId(1L)) } returns 1

                val message = """{"eventId":"evt-1","eventType":"PostLiked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 1) { postStatsCommandPort.incrementLikeCount(PostId(1L)) }
            }

            it("should throw when incrementLikeCount affects 0 rows") {
                every { processedEventPort.markProcessed("evt-1", "PostLiked") } returns true
                every { postStatsCommandPort.incrementLikeCount(PostId(1L)) } returns 0

                val message = """{"eventId":"evt-1","eventType":"PostLiked","postId":{"value":1}}"""
                shouldThrow<IllegalStateException> {
                    consumer.consume(message)
                }
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-1", "PostLiked") } returns false

                val message = """{"eventId":"evt-1","eventType":"PostLiked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementLikeCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementLikeCount(any()) }
            }
        }

        context("PostUnliked event") {

            it("should decrement like count when event is newly processed") {
                every { processedEventPort.markProcessed("evt-2", "PostUnliked") } returns true
                every { postStatsCommandPort.decrementLikeCount(PostId(1L)) } returns 1

                val message = """{"eventId":"evt-2","eventType":"PostUnliked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 1) { postStatsCommandPort.decrementLikeCount(PostId(1L)) }
            }

            it("should throw when decrementLikeCount affects 0 rows") {
                every { processedEventPort.markProcessed("evt-2", "PostUnliked") } returns true
                every { postStatsCommandPort.decrementLikeCount(PostId(1L)) } returns 0

                val message = """{"eventId":"evt-2","eventType":"PostUnliked","postId":{"value":1}}"""
                shouldThrow<IllegalStateException> {
                    consumer.consume(message)
                }
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-3", "PostUnliked") } returns false

                val message = """{"eventId":"evt-3","eventType":"PostUnliked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementLikeCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementLikeCount(any()) }
            }
        }

        context("unknown event type") {

            it("should not interact with post stats for unknown event types") {
                every { processedEventPort.markProcessed("evt-4", "PostBookmarked") } returns true

                val message = """{"eventId":"evt-4","eventType":"PostBookmarked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementLikeCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementLikeCount(any()) }
            }
        }

        context("OpenSearch sync after PostLiked") {
            it("should call updateStats with current stats after increment") {
                every { processedEventPort.markProcessed("evt-sync", "PostLiked") } returns true
                every { postStatsCommandPort.incrementLikeCount(PostId(1L)) } returns 1
                every { postStatsQueryPort.findByPostId(PostId(1L)) } returns
                    PostStats(postId = PostId(1L), likeCount = 3, commentCount = 5, viewCount = 0)

                val message = """{"eventId":"evt-sync","eventType":"PostLiked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 1) { postIndexWriter.updateStats(1L, 3, 5) }
            }

            it("should not call updateStats when stats row is missing") {
                every { processedEventPort.markProcessed("evt-null", "PostLiked") } returns true
                every { postStatsCommandPort.incrementLikeCount(PostId(1L)) } returns 1
                every { postStatsQueryPort.findByPostId(PostId(1L)) } returns null

                val message = """{"eventId":"evt-null","eventType":"PostLiked","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postIndexWriter.updateStats(any(), any(), any()) }
            }
        }
    }
})

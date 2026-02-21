package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.port.PostStatsCommandPort
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*

class CommentEventConsumerSpec : DescribeSpec({

    val postStatsCommandPort = mockk<PostStatsCommandPort>()
    val processedEventPort = mockk<ProcessedEventPort>()
    val objectMapper: ObjectMapper = jacksonObjectMapper()
    val consumer = CommentEventConsumer(postStatsCommandPort, processedEventPort, objectMapper)

    beforeEach {
        clearMocks(postStatsCommandPort, processedEventPort)
    }

    describe("CommentEventConsumer") {

        context("CommentCreated event") {

            it("should increment comment count when event is newly processed") {
                every { processedEventPort.markProcessed("evt-1", "CommentCreated") } returns true
                every { postStatsCommandPort.incrementCommentCount(PostId(1L)) } returns 1

                val message = """{"eventId":"evt-1","eventType":"CommentCreated","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 1) { postStatsCommandPort.incrementCommentCount(PostId(1L)) }
            }

            it("should throw when incrementCommentCount affects 0 rows") {
                every { processedEventPort.markProcessed("evt-1", "CommentCreated") } returns true
                every { postStatsCommandPort.incrementCommentCount(PostId(1L)) } returns 0

                val message = """{"eventId":"evt-1","eventType":"CommentCreated","postId":{"value":1}}"""
                shouldThrow<IllegalStateException> {
                    consumer.consume(message)
                }
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-1", "CommentCreated") } returns false

                val message = """{"eventId":"evt-1","eventType":"CommentCreated","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementCommentCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementCommentCount(any()) }
            }
        }

        context("CommentDeleted event") {

            it("should decrement comment count when event is newly processed") {
                every { processedEventPort.markProcessed("evt-2", "CommentDeleted") } returns true
                every { postStatsCommandPort.decrementCommentCount(PostId(1L)) } returns 1

                val message = """{"eventId":"evt-2","eventType":"CommentDeleted","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 1) { postStatsCommandPort.decrementCommentCount(PostId(1L)) }
            }

            it("should throw when decrementCommentCount affects 0 rows") {
                every { processedEventPort.markProcessed("evt-2", "CommentDeleted") } returns true
                every { postStatsCommandPort.decrementCommentCount(PostId(1L)) } returns 0

                val message = """{"eventId":"evt-2","eventType":"CommentDeleted","postId":{"value":1}}"""
                shouldThrow<IllegalStateException> {
                    consumer.consume(message)
                }
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-3", "CommentDeleted") } returns false

                val message = """{"eventId":"evt-3","eventType":"CommentDeleted","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementCommentCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementCommentCount(any()) }
            }
        }

        context("unknown event type") {

            it("should not interact with post stats for unknown event types") {
                every { processedEventPort.markProcessed("evt-4", "CommentEdited") } returns true

                val message = """{"eventId":"evt-4","eventType":"CommentEdited","postId":{"value":1}}"""
                consumer.consume(message)

                verify(exactly = 0) { postStatsCommandPort.incrementCommentCount(any()) }
                verify(exactly = 0) { postStatsCommandPort.decrementCommentCount(any()) }
            }
        }
    }
})

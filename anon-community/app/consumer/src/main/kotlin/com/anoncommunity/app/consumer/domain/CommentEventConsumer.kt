package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.port.PostStatsCommandPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import com.anoncommunity.infra.kafka.KafkaTopics
import com.anoncommunity.infra.opensearch.adapter.PostIndexWriter
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommentEventConsumer(
    private val postStatsCommandPort: PostStatsCommandPort,
    private val postStatsQueryPort: PostStatsQueryPort,
    private val postIndexWriter: PostIndexWriter,
    private val processedEventPort: ProcessedEventPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [KafkaTopics.DOMAIN_COMMENT],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    @Transactional
    fun consume(message: String) {
        try {
            val node = objectMapper.readTree(message)
            val eventId = node.path("eventId").asText()
            val eventType = node.path("eventType").asText()

            if (!processedEventPort.markProcessed(eventId, eventType)) {
                log.debug("Event already processed: eventId={}", eventId)
                return
            }

            val postId = PostId(node.path("postId").path("value").asLong())

            when (eventType) {
                EVENT_COMMENT_CREATED -> {
                    val affected = postStatsCommandPort.incrementCommentCount(postId)
                    check(affected > 0) { "No post_stats row found for postId=${postId.value}, eventId=$eventId" }
                    log.info("Incremented comment count for post: id={}", postId.value)
                    val stats = postStatsQueryPort.findByPostId(postId)
                    if (stats != null) {
                        postIndexWriter.updateStats(postId.value, stats.likeCount, stats.commentCount)
                    }
                }
                EVENT_COMMENT_DELETED -> {
                    val affected = postStatsCommandPort.decrementCommentCount(postId)
                    check(affected > 0) { "No post_stats row found for postId=${postId.value}, eventId=$eventId" }
                    log.info("Decremented comment count for post: id={}", postId.value)
                    val stats = postStatsQueryPort.findByPostId(postId)
                    if (stats != null) {
                        postIndexWriter.updateStats(postId.value, stats.likeCount, stats.commentCount)
                    }
                }
                else -> log.warn("Unknown comment event type: eventType={}, eventId={}", eventType, eventId)
            }
        } catch (e: Exception) {
            log.error("Failed to process comment domain event", e)
            throw e
        }
    }

    private companion object {
        const val EVENT_COMMENT_CREATED = "CommentCreated"
        const val EVENT_COMMENT_DELETED = "CommentDeleted"
    }
}

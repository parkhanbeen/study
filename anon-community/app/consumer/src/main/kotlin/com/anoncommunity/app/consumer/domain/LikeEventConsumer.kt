package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.port.PostStatsCommandPort
import com.anoncommunity.infra.kafka.KafkaTopics
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeEventConsumer(
    private val postStatsCommandPort: PostStatsCommandPort,
    private val processedEventPort: ProcessedEventPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [KafkaTopics.DOMAIN_LIKE],
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
                EVENT_POST_LIKED -> {
                    val affected = postStatsCommandPort.incrementLikeCount(postId)
                    check(affected > 0) { "No post_stats row found for postId=${postId.value}" }
                    log.info("Incremented like count for post: id={}", postId.value)
                }
                EVENT_POST_UNLIKED -> {
                    val affected = postStatsCommandPort.decrementLikeCount(postId)
                    check(affected > 0) { "No post_stats row found for postId=${postId.value}" }
                    log.info("Decremented like count for post: id={}", postId.value)
                }
                else -> log.warn("Unknown like event type: eventType={}, eventId={}", eventType, eventId)
            }
        } catch (e: Exception) {
            log.error("Failed to process like domain event", e)
            throw e
        }
    }

    private companion object {
        const val EVENT_POST_LIKED = "PostLiked"
        const val EVENT_POST_UNLIKED = "PostUnliked"
    }
}

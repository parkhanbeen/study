package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import com.anoncommunity.infra.kafka.KafkaTopics
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PostEventConsumer(
    private val profileQueryPort: ProfileQueryPort,
    private val profileCommandPort: ProfileCommandPort,
    private val processedEventPort: ProcessedEventPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [KafkaTopics.DOMAIN_POST],
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

            when (eventType) {
                EVENT_POST_CREATED -> {
                    val authorProfileId = node.path("authorProfileId").path("value").asLong()
                    val profile = profileQueryPort.findById(ProfileId(authorProfileId))
                    profile?.let {
                        it.incrementPostCount()
                        profileCommandPort.save(it)
                        log.info("Incremented post count for profile: id={}", authorProfileId)
                    }
                }
                EVENT_POST_DELETED -> {
                    val authorProfileId = node.path("authorProfileId").path("value").asLong()
                    val profile = profileQueryPort.findById(ProfileId(authorProfileId))
                    profile?.let {
                        it.decrementPostCount()
                        profileCommandPort.save(it)
                        log.info("Decremented post count for profile: id={}", authorProfileId)
                    }
                }
                else -> log.warn("Unknown post event type: eventType={}, eventId={}", eventType, eventId)
            }
        } catch (e: Exception) {
            log.error("Failed to process post domain event", e)
            throw e
        }
    }

    private companion object {
        const val EVENT_POST_CREATED = "PostCreated"
        const val EVENT_POST_DELETED = "PostDeleted"
    }
}

package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FollowEventConsumer(
    private val profileCommandPort: ProfileCommandPort,
    private val processedEventPort: ProcessedEventPort,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["anon-community-cdc.anon_community.profile_follows"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    @Transactional
    fun consume(message: String?) {
        if (message == null) {
            log.debug("Received tombstone message, skipping")
            return
        }
        try {
            val root = objectMapper.readTree(message)

            // ExtractNewRecordState 적용 후 flattened 포맷
            val op = root.path("__op").asText("")
            val deleted = root.path("__deleted").asText("false")

            val isDelete = op == "d" || deleted == "true"
            val isInsert = op == "c"

            if (!isInsert && !isDelete) return  // snapshot read(r), update(u) 무시

            val rowId = root.path("id").asLong()
            val followerProfileId = ProfileId(root.path("follower_profile_id").asLong())
            val followingProfileId = ProfileId(root.path("following_profile_id").asLong())

            if (isInsert) {
                val eventId = "${rowId}_c"
                if (!processedEventPort.markProcessed(eventId, "FollowCreated")) return
                profileCommandPort.incrementFollowerCount(followingProfileId)
                profileCommandPort.incrementFollowingCount(followerProfileId)
            } else {
                val eventId = "${rowId}_d"
                if (!processedEventPort.markProcessed(eventId, "FollowDeleted")) return
                profileCommandPort.decrementFollowerCount(followingProfileId)
                profileCommandPort.decrementFollowingCount(followerProfileId)
            }
        } catch (e: Exception) {
            log.error("Failed to process CDC message for profile_follows", e)
            throw e
        }
    }
}

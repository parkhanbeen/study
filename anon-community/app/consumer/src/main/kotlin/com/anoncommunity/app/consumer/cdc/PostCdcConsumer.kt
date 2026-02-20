package com.anoncommunity.app.consumer.cdc

import com.anoncommunity.infra.kafka.KafkaTopics
import com.anoncommunity.infra.opensearch.util.ChosungExtractor
import com.anoncommunity.infra.opensearch.adapter.PostIndexWriter
import com.anoncommunity.infra.opensearch.document.PostDocument
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PostCdcConsumer(
    private val postIndexWriter: PostIndexWriter,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [KafkaTopics.CDC_POSTS],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun consume(message: String?) {
        if (message == null) {
            log.debug("Received tombstone message, skipping")
            return
        }

        try {
            val root = objectMapper.readTree(message)

            // Support both Debezium envelope format (before/after/op) and
            // unwrapped/flattened format (__op/__deleted + flat row columns).
            // Detect envelope format by checking for "op" field at top level with "before"/"after".
            val (node, isDelete) = extractPayload(root)

            if (isDelete) {
                val id = node.path("id").asLong()
                log.info("CDC delete event for post: id={}", id)
                postIndexWriter.delete(id)
                return
            }

            val document = toPostDocument(node)

            // Soft-deleted or removed posts should be removed from search index
            if (document.status in EXCLUDED_STATUSES) {
                log.info("CDC soft-delete event for post: id={}, status={}", document.id, document.status)
                postIndexWriter.delete(document.id)
                return
            }

            log.info("CDC upsert event for post: id={}", document.id)
            postIndexWriter.index(document)
        } catch (e: Exception) {
            log.error("Failed to process CDC message for posts", e)
            throw e
        }
    }

    /**
     * Extracts the row payload and delete flag from either:
     * - Envelope format: { "before": {...}, "after": {...}, "op": "c|u|d|r" }
     * - Unwrapped format: { "id": ..., "__op": "c|u|d", "__deleted": "true|false", ... }
     */
    private data class CdcPayload(val node: JsonNode, val isDelete: Boolean)

    private fun extractPayload(root: JsonNode): CdcPayload {
        val opField = root.path("op")
        val afterField = root.path("after")

        // Envelope format: has "op" (single char) without "__op"
        if (!opField.isMissingNode && root.path("__op").isMissingNode) {
            val op = opField.asText()
            if (op == "d") {
                val before = root.path("before")
                return CdcPayload(node = before, isDelete = true)
            }
            return CdcPayload(node = afterField, isDelete = false)
        }

        // Unwrapped (flattened) format
        val flatOp = root.path("__op").asText("")
        val deleted = root.path("__deleted").asText("false")
        val isDelete = flatOp == "d" || deleted == "true"
        return CdcPayload(node = root, isDelete = isDelete)
    }

    private fun toPostDocument(node: JsonNode): PostDocument {
        val tags = node.path("tags").let { tagsNode ->
            if (tagsNode.isNull || tagsNode.isMissingNode) emptyList()
            else {
                try {
                    val parsed = objectMapper.readTree(tagsNode.asText())
                    if (parsed.isArray) parsed.map { it.asText() } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        val title = node.path("title").asText("")
        val content = node.path("content").asText("")

        return PostDocument(
            id = node.path("id").asLong(),
            authorId = node.path("author_id").asLong(),
            title = title,
            content = content,
            category = node.path("category").asText("DAILY"),
            tags = tags,
            status = node.path("status").asText("PUBLISHED"),
            createdAt = parseTimestamp(node.path("created_at")),
            updatedAt = parseTimestamp(node.path("updated_at")),
            titleChosung = ChosungExtractor.extractChosung(title),
            contentChosung = ChosungExtractor.extractChosung(content),
        )
    }

    companion object {
        private val EXCLUDED_STATUSES = setOf("DELETED", "REMOVED")
    }

    private fun parseTimestamp(node: JsonNode): Instant {
        if (node.isNull || node.isMissingNode) {
            log.warn("Timestamp node is null or missing, falling back to Instant.now()")
            return Instant.now()
        }
        return try {
            if (node.isLong) Instant.ofEpochMilli(node.asLong())
            else Instant.parse(node.asText())
        } catch (e: Exception) {
            log.warn("Failed to parse timestamp: value={}, error={}", node, e.message)
            Instant.now()
        }
    }
}

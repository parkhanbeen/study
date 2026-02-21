package com.anoncommunity.infra.opensearch.adapter

import com.anoncommunity.infra.opensearch.document.PostDocument
import com.anoncommunity.infra.opensearch.index.PostIndexMapping
import com.fasterxml.jackson.databind.ObjectMapper
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class PostIndexWriter(
    private val client: RestHighLevelClient,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val INDEX_WRITE = PostIndexMapping.INDEX_NAME
        const val INDEX_READ = PostIndexMapping.ALIAS_NAME
    }

    fun index(document: PostDocument) {
        val json = objectMapper.writeValueAsString(document)
        val request = IndexRequest(INDEX_WRITE)
            .id(document.id.toString())
            .source(json, XContentType.JSON)
        try {
            client.index(request, RequestOptions.DEFAULT)
            log.debug("Indexed post document: id={}", document.id)
        } catch (e: IOException) {
            log.error("Failed to index post document: id={}", document.id, e)
            throw e
        }
    }

    fun delete(postId: Long) {
        val request = DeleteRequest(INDEX_WRITE, postId.toString())
        try {
            client.delete(request, RequestOptions.DEFAULT)
            log.debug("Deleted post document: id={}", postId)
        } catch (e: IOException) {
            log.error("Failed to delete post document: id={}", postId, e)
            throw e
        }
    }
}

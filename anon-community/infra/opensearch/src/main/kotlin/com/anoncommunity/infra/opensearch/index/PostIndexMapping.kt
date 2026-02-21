package com.anoncommunity.infra.opensearch.index

import org.opensearch.OpenSearchStatusException
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.opensearch.index.reindex.ReindexRequest
import org.opensearch.core.rest.RestStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PostIndexMapping(
    private val client: RestHighLevelClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun ensureIndex() {
        try {
            createIndexIfNotExists()
            migrateAndSwapAlias()
        } catch (e: Exception) {
            // Startup은 계속 진행 — search만 degraded mode
            log.error("Failed to initialize OpenSearch index. Search may be unavailable until resolved.", e)
        }
    }

    private fun createIndexIfNotExists() {
        if (indexExists(INDEX_NAME)) {
            log.info("OpenSearch index '{}' already exists", INDEX_NAME)
            return
        }
        try {
            val request = CreateIndexRequest(INDEX_NAME)
            request.source(INDEX_SETTINGS, XContentType.JSON)
            client.indices().create(request, RequestOptions.DEFAULT)
            log.info("Created OpenSearch index '{}'", INDEX_NAME)
        } catch (e: OpenSearchStatusException) {
            // 다른 pod이 동시에 생성한 경우 (rolling deployment)
            if (e.status() == RestStatus.BAD_REQUEST && e.message?.contains("already_exists") == true) {
                log.info("Index '{}' was created by another instance, continuing", INDEX_NAME)
            } else {
                throw e
            }
        }
    }

    private fun migrateAndSwapAlias() {
        val currentTargets = findAliasTargets()

        if (currentTargets == setOf(INDEX_NAME)) {
            log.info("Alias '{}' already points to '{}'", ALIAS_NAME, INDEX_NAME)
            return
        }

        // Reindex from old indices before swapping alias
        val oldIndices = currentTargets.filter { it != INDEX_NAME }
        var reindexSuccess = true
        oldIndices.forEach { oldIndex ->
            try {
                reindexData(oldIndex)
            } catch (e: Exception) {
                log.error("Reindex from '{}' failed, skipping alias swap for safety", oldIndex, e)
                reindexSuccess = false
            }
        }

        if (!reindexSuccess) {
            log.warn("Reindex incomplete — alias swap aborted. Manual migration required.")
            return
        }

        // Atomic alias swap: REMOVE from old + ADD to new in single request
        val aliasRequest = IndicesAliasesRequest()
        oldIndices.forEach { oldIndex ->
            aliasRequest.addAliasAction(
                IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(oldIndex)
                    .alias(ALIAS_NAME)
            )
            log.info("Removing alias '{}' from old index '{}'", ALIAS_NAME, oldIndex)
        }

        if (INDEX_NAME !in currentTargets) {
            aliasRequest.addAliasAction(
                IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                    .index(INDEX_NAME)
                    .alias(ALIAS_NAME)
            )
        }

        client.indices().updateAliases(aliasRequest, RequestOptions.DEFAULT)
        log.info("Alias '{}' now points to '{}'", ALIAS_NAME, INDEX_NAME)
    }

    private fun reindexData(sourceIndex: String) {
        if (!indexExists(sourceIndex)) {
            log.warn("Source index '{}' does not exist, skipping reindex", sourceIndex)
            return
        }

        log.info("Reindexing from '{}' to '{}'...", sourceIndex, INDEX_NAME)
        val request = ReindexRequest()
        request.setSourceIndices(sourceIndex)
        request.setDestIndex(INDEX_NAME)
        request.setDestOpType("create")
        request.setConflicts("proceed")
        request.setSlices(REINDEX_SLICES)

        val response = client.reindex(request, RequestOptions.DEFAULT)

        if (response.bulkFailures.isNotEmpty()) {
            log.error(
                "Reindex had {} bulk failures. samples={}",
                response.bulkFailures.size,
                response.bulkFailures.take(3).joinToString { it.message },
            )
            throw IllegalStateException("Reindex from '$sourceIndex' completed with ${response.bulkFailures.size} bulk failures")
        }

        log.info(
            "Reindex complete: {} → {}, created={}, conflicts={}",
            sourceIndex, INDEX_NAME, response.created, response.versionConflicts,
        )
    }

    private fun findAliasTargets(): Set<String> {
        return try {
            val response = client.indices().getAlias(GetAliasesRequest(ALIAS_NAME), RequestOptions.DEFAULT)
            response.aliases.keys
        } catch (e: OpenSearchStatusException) {
            if (e.status() == RestStatus.NOT_FOUND) {
                log.info("Alias '{}' does not exist yet", ALIAS_NAME)
                emptySet()
            } else {
                throw e
            }
        }
    }

    private fun indexExists(name: String): Boolean =
        client.indices().exists(GetIndexRequest(name), RequestOptions.DEFAULT)

    companion object {
        const val INDEX_NAME = "posts-v5"
        const val ALIAS_NAME = "posts-read"
        private const val REINDEX_SLICES = 4

        internal val INDEX_SETTINGS = """
        {
          "settings": {
            "number_of_shards": 3,
            "number_of_replicas": 1,
            "refresh_interval": "30s",
            "analysis": {
              "analyzer": {
                "nori_analyzer": {
                  "type": "custom",
                  "tokenizer": "nori_mixed",
                  "filter": ["lowercase", "nori_part_of_speech", "korean_stopwords", "nori_readingform"]
                },
                "nori_search_analyzer": {
                  "type": "custom",
                  "tokenizer": "nori_mixed",
                  "filter": ["lowercase", "nori_part_of_speech", "korean_stopwords", "synonym_filter", "nori_readingform"]
                },
                "ngram_analyzer": {
                  "type": "custom",
                  "tokenizer": "ngram_tokenizer",
                  "filter": ["lowercase"]
                },
                "icu_analyzer": {
                  "type": "custom",
                  "tokenizer": "icu_tokenizer",
                  "filter": ["icu_normalizer", "icu_folding", "lowercase"]
                }
              },
              "tokenizer": {
                "nori_mixed": {
                  "type": "nori_tokenizer",
                  "decompound_mode": "mixed",
                  "user_dictionary": "nori_userdict.txt"
                },
                "ngram_tokenizer": {
                  "type": "ngram",
                  "min_gram": 2,
                  "max_gram": 3,
                  "token_chars": ["letter", "digit"]
                }
              },
              "filter": {
                "nori_part_of_speech": {
                  "type": "nori_part_of_speech",
                  "stoptags": ["E", "IC", "J", "MAG", "MAJ", "SC", "SE", "SF", "SP", "SSC", "SSO", "SY", "XPN", "XSA", "XSN", "XSV"]
                },
                "korean_stopwords": {
                  "type": "stop",
                  "stopwords": ["의", "가", "이", "은", "는", "을", "를", "에", "에서", "로", "으로", "와", "과", "도", "만", "까지", "부터", "이다", "이것", "저것", "그것", "것", "등", "들", "좀", "잘", "못", "안"]
                },
                "synonym_filter": {
                  "type": "synonym",
                  "synonyms_path": "synonyms.txt",
                  "lenient": true
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "id": { "type": "long" },
              "authorProfileId": { "type": "long" },
              "category": { "type": "keyword" },
              "title": {
                "type": "text",
                "analyzer": "nori_analyzer",
                "search_analyzer": "nori_search_analyzer",
                "fields": {
                  "standard": { "type": "text", "analyzer": "standard" },
                  "ngram": { "type": "text", "analyzer": "ngram_analyzer", "search_analyzer": "nori_analyzer" },
                  "icu": { "type": "text", "analyzer": "icu_analyzer" }
                }
              },
              "content": {
                "type": "text",
                "analyzer": "nori_analyzer",
                "search_analyzer": "nori_search_analyzer",
                "fields": {
                  "standard": { "type": "text", "analyzer": "standard" },
                  "ngram": { "type": "text", "analyzer": "ngram_analyzer", "search_analyzer": "nori_analyzer" },
                  "icu": { "type": "text", "analyzer": "icu_analyzer" }
                }
              },
              "tags": {
                "type": "keyword",
                "fields": {
                  "text": { "type": "text", "analyzer": "nori_analyzer", "search_analyzer": "nori_search_analyzer" },
                  "ngram": { "type": "text", "analyzer": "ngram_analyzer" }
                }
              },
              "status": { "type": "keyword" },
              "createdAt": { "type": "date" },
              "updatedAt": { "type": "date" },
              "commentCount": { "type": "integer" },
              "likeCount": { "type": "integer" },
              "viewCount": { "type": "integer" },
              "titleChosung": { "type": "keyword" },
              "contentChosung": { "type": "keyword" }
            }
          }
        }
        """.trimIndent()
    }
}

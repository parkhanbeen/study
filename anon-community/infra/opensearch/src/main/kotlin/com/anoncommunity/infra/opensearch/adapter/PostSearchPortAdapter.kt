package com.anoncommunity.infra.opensearch.adapter

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.SortDirection
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostCategory
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStatus
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.Tag
import com.anoncommunity.domain.post.port.CursorPageResult
import com.anoncommunity.domain.post.port.CursorSearchQuery
import com.anoncommunity.domain.post.port.PostFeedPort
import com.anoncommunity.domain.post.port.PostSearchHit
import com.anoncommunity.domain.post.port.PostSearchPort
import com.anoncommunity.domain.post.port.PostSearchQuery
import com.anoncommunity.infra.opensearch.document.PostDocument
import com.anoncommunity.infra.opensearch.exception.SearchExecutionException
import com.fasterxml.jackson.databind.ObjectMapper
import org.opensearch.action.search.SearchRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.common.unit.TimeValue
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.MultiMatchQueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.aggregations.AggregationBuilders
import org.opensearch.search.aggregations.bucket.terms.Terms
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.fetch.subphase.highlight.HighlightBuilder
import org.opensearch.search.sort.SortOrder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PostSearchPortAdapter(
    private val client: RestHighLevelClient,
    private val objectMapper: ObjectMapper,
) : PostSearchPort, PostFeedPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val ALLOWED_SORT_FIELDS = setOf("createdAt", "likeCount", "commentCount", "viewCount", "updatedAt")

    override fun search(query: PostSearchQuery): PageResult<Post> {
        val boolQuery = buildSearchQuery(query.keyword, query.tags, query.category, query.language)
        return executeSearch(boolQuery, query.pageable)
    }

    override fun searchWithCursor(query: CursorSearchQuery): CursorPageResult<Post> {
        val boolQuery = buildSearchQuery(query.keyword, query.tags, query.category, query.language)
        applyDateRangeFilter(boolQuery, query.dateRange)
        return executeCursorSearch(boolQuery, query.size, query.searchAfter, query.sortBy, query.keyword != null)
    }

    override fun searchHitsWithCursor(query: CursorSearchQuery): CursorPageResult<PostSearchHit> {
        val boolQuery = buildSearchQuery(query.keyword, query.tags, query.category, query.language)
        applyDateRangeFilter(boolQuery, query.dateRange)

        val highlighter = buildHighlighter()

        val searchSource = SearchSourceBuilder()
            .query(boolQuery)
            .size(query.size)
            .timeout(SEARCH_TIMEOUT)
            .highlighter(highlighter)

        applySort(searchSource, query.sortBy, query.keyword != null)
        query.searchAfter?.let { searchSource.searchAfter(it.toTypedArray()) }

        val request = SearchRequest(PostIndexWriter.INDEX_READ).source(searchSource)

        try {
            val response = client.search(request, RequestOptions.DEFAULT)
            val hits = response.hits
            val searchHits = hits.hits.mapNotNull { hit ->
                mapToPostSafely(hit.sourceAsString)?.let { post ->
                    val highlights = hit.highlightFields.mapValues { (_, field) ->
                        field.fragments().map { it.string() }
                    }
                    PostSearchHit(post = post, highlights = highlights)
                }
            }

            val lastSortValues = if (hits.hits.isNotEmpty()) hits.hits.last().sortValues.toList() else null

            return CursorPageResult(
                content = searchHits,
                size = query.size,
                searchAfter = lastSortValues,
                hasNext = hits.hits.size == query.size,
            )
        } catch (e: Exception) {
            log.error("Failed to execute hits cursor search: size={}, searchAfter={}", query.size, query.searchAfter, e)
            throw SearchExecutionException("Failed to search posts with highlights", e)
        }
    }

    override fun getFeed(category: String?, pageable: DomainPageable): PageResult<Post> {
        val boolQuery = BoolQueryBuilder()
        boolQuery.filter(QueryBuilders.termQuery("status", "PUBLISHED"))
        category?.let { boolQuery.filter(QueryBuilders.termQuery("category", it)) }
        return executeSearch(boolQuery, pageable)
    }

    override fun getHotTags(limit: Int): List<String> {
        val boolQuery = BoolQueryBuilder()
        boolQuery.filter(QueryBuilders.termQuery("status", "PUBLISHED"))

        val agg = AggregationBuilders.terms("hot_tags")
            .field("tags")
            .size(minOf(limit + 5, 15))
            .minDocCount(3)

        val searchSource = SearchSourceBuilder()
            .query(boolQuery)
            .size(0)
            .timeout(SEARCH_TIMEOUT)
            .aggregation(agg)

        val request = SearchRequest(PostIndexWriter.INDEX_READ).source(searchSource)

        return try {
            val response = client.search(request, RequestOptions.DEFAULT)
            val terms = response.aggregations?.get<Terms>("hot_tags") ?: return emptyList()
            terms.buckets.take(limit).map { it.keyAsString }
        } catch (e: Exception) {
            log.error("Failed to fetch hot tags", e)
            emptyList()
        }
    }

    private fun buildHighlighter(): HighlightBuilder =
        HighlightBuilder()
            .field("title")
            .field("content")
            .preTags("<em>")
            .postTags("</em>")
            .fragmentSize(150)
            .numOfFragments(1)
            .requireFieldMatch(false)

    private fun applySort(searchSource: SearchSourceBuilder, sortBy: String, hasKeyword: Boolean) {
        val validSort = if (sortBy in ALLOWED_SORT_FIELDS) sortBy else "createdAt"
        when {
            validSort == "createdAt" -> {
                searchSource.sort("createdAt", SortOrder.DESC)
                searchSource.sort("_id", SortOrder.DESC)
            }
            else -> {
                searchSource.sort(validSort, SortOrder.DESC)
                searchSource.sort("createdAt", SortOrder.DESC)
                searchSource.sort("_id", SortOrder.DESC)
            }
        }
    }

    private fun applyDateRangeFilter(boolQuery: BoolQueryBuilder, dateRange: String?) {
        if (dateRange == null) return
        val now = Instant.now()
        val from: Instant = when (dateRange) {
            "today" -> now.truncatedTo(ChronoUnit.DAYS)
            "this_week" -> now.minus(7, ChronoUnit.DAYS)
            "this_month" -> now.minus(30, ChronoUnit.DAYS)
            else -> return
        }
        boolQuery.filter(
            QueryBuilders.rangeQuery("createdAt")
                .gte(from.toEpochMilli())
                .lte(now.toEpochMilli())
                .format("epoch_millis")
        )
    }

    private fun buildSearchQuery(
        keyword: String?,
        tags: List<Tag>?,
        category: String?,
        language: String?,
    ): BoolQueryBuilder {
        val boolQuery = BoolQueryBuilder()
        boolQuery.filter(QueryBuilders.termQuery("status", "PUBLISHED"))

        keyword?.let { kw ->
            val mainQuery = QueryBuilders.multiMatchQuery(kw)
                .field("title", 3.0f)
                .field("title.standard", 1.5f)
                .field("title.icu", 2.0f)
                .field("content", 2.0f)
                .field("content.standard")
                .field("content.icu", 1.5f)
                .field("tags.text", 1.0f)
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .minimumShouldMatch(dynamicMinimumShouldMatch(kw))

            val phraseBoostQuery = QueryBuilders.multiMatchQuery(kw)
                .field("title", 2.0f)
                .field("content", 1.0f)
                .type(MultiMatchQueryBuilder.Type.PHRASE)
                .slop(1)

            val ngramQuery = QueryBuilders.multiMatchQuery(kw)
                .field("title.ngram", 0.3f)
                .field("content.ngram", 0.3f)
                .field("tags.ngram", 0.2f)
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)

            boolQuery.must(mainQuery)
            boolQuery.should(phraseBoostQuery)
            boolQuery.should(ngramQuery)
        }

        category?.let { cat ->
            boolQuery.filter(QueryBuilders.termQuery("category", cat))
        }

        tags?.forEach { tag ->
            boolQuery.filter(QueryBuilders.termQuery("tags", tag.value))
        }

        return boolQuery
    }

    private fun executeSearch(query: BoolQueryBuilder, pageable: DomainPageable): PageResult<Post> {
        val searchSource = SearchSourceBuilder()
            .query(query)
            .from(pageable.page * pageable.size)
            .size(pageable.size)
            .timeout(SEARCH_TIMEOUT)
            .sort(
                pageable.sortBy,
                when (pageable.sortDirection) {
                    SortDirection.ASC -> SortOrder.ASC
                    SortDirection.DESC -> SortOrder.DESC
                },
            )

        val request = SearchRequest(PostIndexWriter.INDEX_READ).source(searchSource)

        try {
            val response = client.search(request, RequestOptions.DEFAULT)
            val hits = response.hits
            val posts = hits.hits.mapNotNull { hit -> mapToPostSafely(hit.sourceAsString) }

            val totalHits = hits.totalHits?.value ?: 0L
            val totalPages = if (pageable.size > 0) ((totalHits + pageable.size - 1) / pageable.size).toInt() else 0

            return PageResult(
                content = posts,
                page = pageable.page,
                size = pageable.size,
                totalElements = totalHits,
                totalPages = totalPages,
            )
        } catch (e: Exception) {
            log.error("Failed to execute search query: page={}, size={}", pageable.page, pageable.size, e)
            throw SearchExecutionException("Failed to search posts", e)
        }
    }

    private fun executeCursorSearch(
        query: BoolQueryBuilder,
        size: Int,
        searchAfter: List<Any>?,
        sortBy: String = "createdAt",
        hasKeyword: Boolean = false,
    ): CursorPageResult<Post> {
        val searchSource = SearchSourceBuilder()
            .query(query)
            .size(size)
            .timeout(SEARCH_TIMEOUT)

        applySort(searchSource, sortBy, hasKeyword)
        searchAfter?.let { searchSource.searchAfter(it.toTypedArray()) }

        val request = SearchRequest(PostIndexWriter.INDEX_READ).source(searchSource)

        try {
            val response = client.search(request, RequestOptions.DEFAULT)
            val hits = response.hits
            val posts = hits.hits.mapNotNull { hit -> mapToPostSafely(hit.sourceAsString) }

            val lastSortValues = if (hits.hits.isNotEmpty()) {
                hits.hits.last().sortValues.toList()
            } else null

            return CursorPageResult(
                content = posts,
                size = size,
                searchAfter = lastSortValues,
                hasNext = hits.hits.size == size,
            )
        } catch (e: Exception) {
            log.error("Failed to execute cursor search query: size={}, searchAfter={}", size, searchAfter, e)
            throw SearchExecutionException("Failed to search posts with cursor", e)
        }
    }

    private fun mapToPostSafely(source: String): Post? {
        return try {
            mapToPost(source)
        } catch (e: Exception) {
            log.warn("Failed to parse post document, skipping: {}", e.message)
            null
        }
    }

    private fun mapToPost(source: String): Post {
        val doc = objectMapper.readValue(source, PostDocument::class.java)
        return Post(
            id = PostId(doc.id),
            authorId = MemberId(doc.authorId),
            title = PostTitle(doc.title),
            content = PostContent(doc.content),
            category = runCatching { PostCategory.valueOf(doc.category) }.getOrDefault(PostCategory.DAILY),
            tags = doc.tags.map { Tag(it) },
            status = PostStatus.valueOf(doc.status),
            createdAt = doc.createdAt,
            updatedAt = doc.updatedAt,
        )
    }

    /**
     * Returns minimumShouldMatch percentage based on keyword length.
     */
    private fun dynamicMinimumShouldMatch(keyword: String): String {
        val len = keyword.replace(" ", "").length
        return when {
            len <= 2 -> "100%"
            len <= 5 -> "75%"
            else -> "50%"
        }
    }

    companion object {
        private val SEARCH_TIMEOUT = TimeValue.timeValueSeconds(3)
    }
}

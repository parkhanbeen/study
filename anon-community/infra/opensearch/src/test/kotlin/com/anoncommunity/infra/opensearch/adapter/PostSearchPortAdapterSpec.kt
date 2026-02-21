package com.anoncommunity.infra.opensearch.adapter

import com.anoncommunity.domain.post.port.CursorSearchQuery
import io.kotest.assertions.throwables.shouldNotThrowAny
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.search.SearchResponse
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.search.SearchHits

class PostSearchPortAdapterSpec : DescribeSpec({

    val client = mockk<RestHighLevelClient>()
    val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    val adapter = PostSearchPortAdapter(client, objectMapper)

    val mockHits = mockk<SearchHits>(relaxed = true)
    val mockResponse = mockk<SearchResponse>(relaxed = true)

    beforeSpec {
        every { mockResponse.hits } returns mockHits
        every { mockHits.hits } returns emptyArray()
        every { mockHits.totalHits } returns TotalHits(0L, TotalHits.Relation.EQUAL_TO)
    }

    describe("PostSearchPortAdapter") {

        context("searchWithCursor with keyword") {

            it("should not use field^boost^1.0 double-boost notation that causes HLRC serialization bug") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "테스트", size = 10))

                val queryJson = requestSlot.captured.source().toString()

                val hasDoubleBoost = queryJson.contains(Regex("""\^\d+\.?\d*\^1\.0"""))
                hasDoubleBoost shouldBe false
            }

            it("should include title, content, and tags in the multi_match query") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "검색어", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe true
                queryJson.contains("title") shouldBe true
                queryJson.contains("content") shouldBe true
                queryJson.contains("tags") shouldBe true
            }

            it("should apply minimum_should_match 75% for 3-char keyword") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "검색어", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("75%") shouldBe true
            }
        }

        context("searchWithCursor dynamic minimumShouldMatch") {

            it("should use 100% minimumShouldMatch for 2-char keyword to prevent over-matching") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "근무", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("100%") shouldBe true
            }

            it("should use 50% minimumShouldMatch for 6-char keyword") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "재택근무일상", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("50%") shouldBe true
            }
        }

        context("searchWithCursor query structure") {

            it("should NOT wrap keyword queries in nested bool.should with minimum_should_match 1") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "재택근무", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                val hasRequiredNgramWrapper = queryJson.contains(Regex(""""minimum_should_match"\s*:\s*1"""))
                hasRequiredNgramWrapper shouldBe false
            }

            it("should include phrase multi_match as optional boost for exact phrase relevance") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = "재택근무", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("phrase") shouldBe true
            }
        }

        context("searchHitsWithCursor highlighting") {

            it("should include highlight configuration in search request") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                shouldNotThrowAny {
                    adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "테스트", size = 10))
                }

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("highlight") shouldBe true
            }

            it("should highlight title and content fields") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "테스트", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("title") shouldBe true
                queryJson.contains("content") shouldBe true
            }

            it("should return empty hits without throwing when no results") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                val result = adapter.searchHitsWithCursor(CursorSearchQuery(keyword = null, size = 10))

                result.content shouldBe emptyList()
                result.hasNext shouldBe false
            }
        }

        context("searchWithCursor without keyword") {

            it("should not include multi_match when keyword is null") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = null, size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe false
            }

            it("should still filter by PUBLISHED status when keyword is null") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchWithCursor(CursorSearchQuery(keyword = null, size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("PUBLISHED") shouldBe true
            }
        }

        context("searchHitsWithCursor sortBy options") {
            it("should sort by likeCount before createdAt when sortBy=likeCount") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "test", size = 10, sortBy = "likeCount"))
                val queryJson = requestSlot.captured.source().toString()
                val likeIdx = queryJson.indexOf("likeCount")
                val createdIdx = queryJson.lastIndexOf("createdAt")
                likeIdx shouldBeLessThan createdIdx
            }

            it("should sort by createdAt when sortBy is unknown") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "test", size = 10, sortBy = "unknown"))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("createdAt") shouldBe true
            }

            it("should apply range filter when dateRange=today") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "test", size = 10, dateRange = "today"))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("range") shouldBe true
            }

            it("should NOT apply range filter when dateRange is null") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "test", size = 10, dateRange = null))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("range") shouldBe false
            }
        }

        context("searchWithCursor chosung (initial consonant) query") {

            it("should use wildcard on titleChosung and contentChosung for pure-consonant keyword") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchWithCursor(CursorSearchQuery(keyword = "ㄱㄴㄷ", size = 10))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("wildcard") shouldBe true
                queryJson.contains("titleChosung") shouldBe true
                queryJson.contains("contentChosung") shouldBe true
            }

            it("should NOT use multi_match when keyword is pure consonants") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchWithCursor(CursorSearchQuery(keyword = "ㅈㅊㅋ", size = 10))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe false
            }

            it("should use regular multi_match for normal Korean keyword, not wildcard on chosung fields") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchWithCursor(CursorSearchQuery(keyword = "재택근무", size = 10))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe true
                queryJson.contains("titleChosung") shouldBe false
            }

            it("should NOT treat keyword longer than 6 consonants as chosung query") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchWithCursor(CursorSearchQuery(keyword = "ㄱㄴㄷㄹㅁㅂㅅ", size = 10))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe true
                queryJson.contains("titleChosung") shouldBe false
            }

            it("should include wildcard pattern with asterisk around consonants") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.searchWithCursor(CursorSearchQuery(keyword = "ㅈㄱ", size = 10))
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("*ㅈㄱ*") shouldBe true
            }
        }

        context("초성 검색 (chosung query)") {

            it("should use wildcard query on titleChosung when keyword is all consonants") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "ㅁㅅㅅ", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("wildcard") shouldBe true
                queryJson.contains("titleChosung") shouldBe true
            }

            it("should NOT use multi_match when keyword is all consonants (chosung query)") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "ㄷㅇ", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe false
            }

            it("should NOT activate chosung path when keyword has vowels (e.g. 운동)") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "운동", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("multi_match") shouldBe true
                queryJson.contains("wildcard") shouldBe false
            }

            it("should NOT activate chosung path when chosung query is longer than 6 chars (safety guard)") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "ㄱㄴㄷㄹㅁㅂㅅ", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("wildcard") shouldBe false
            }

            it("should search both titleChosung and contentChosung in chosung query") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse

                adapter.searchHitsWithCursor(CursorSearchQuery(keyword = "ㅇㄷ", size = 10))

                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("titleChosung") shouldBe true
                queryJson.contains("contentChosung") shouldBe true
            }
        }

        context("getHotTags") {
            it("should send aggregation query to opensearch") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.getHotTags(limit = 10)
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("aggregations") shouldBe true
                queryJson.contains("terms") shouldBe true
            }

            it("should filter only PUBLISHED documents in hot-tags query") {
                val requestSlot = slot<SearchRequest>()
                every { client.search(capture(requestSlot), any<RequestOptions>()) } returns mockResponse
                adapter.getHotTags(limit = 10)
                val queryJson = requestSlot.captured.source().toString()
                queryJson.contains("PUBLISHED") shouldBe true
            }

            it("should return empty list when no aggregation buckets") {
                every { client.search(any(), any<RequestOptions>()) } returns mockResponse
                val result = adapter.getHotTags(limit = 10)
                result shouldBe emptyList()
            }
        }
    }
})

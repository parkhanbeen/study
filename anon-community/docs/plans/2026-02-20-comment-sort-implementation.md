# Comment Sort (댓글 많은 순) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add "댓글 많은 순" (most commented) sort option to feed, enabling users to discover posts with the most discussion activity.

**Architecture:** Denormalize commentCount into PostDocument (OpenSearch), sync via CDC events (CommentCreated/CommentDeleted → PostIndexWriter.updateCommentCount), extend backend ALLOWED_SORT_FIELDS, add frontend dropdown option. Eventual consistency model (~1 second lag).

**Tech Stack:** OpenSearch 2.x (denormalized commentCount field), Debezium + Kafka (CDC events), Kotlin + Spring Boot (backend), Next.js 16 + React Query (frontend), Kotest 5.9.1 (testing)

**Design doc:** `docs/plans/2026-02-20-comment-sort-design.md`

---

## Current State

**Backend:**
- FeedController ALLOWED_SORT_FIELDS: `["createdAt", "updatedAt"]` (missing "commentCount")
- PostDocument fields: id, authorId, title, content, category, tags, status, createdAt, updatedAt (missing commentCount)
- CommentEventConsumer: publishes CommentCreated/CommentDeleted events, no OpenSearch sync yet
- PostIndexWriter: has createOrUpdate, delete methods, no updateCommentCount method

**Frontend:**
- FeedSort type: `"latest" | "oldest"` (missing "popular")
- Dropdown options: 최신순, 오래된순 (missing 댓글 많은 순)
- API mapping: latest → createdAt/DESC, oldest → createdAt/ASC

**Missing:**
- PostDocument.commentCount field
- OpenSearch mapping for commentCount
- PostIndexWriter.updateCommentCount method
- CommentEventConsumer → OpenSearch sync logic
- Backend "commentCount" sort support
- Frontend "popular" sort option
- Initial data migration for existing commentCounts

---

## Task 0: Add Kotest test dependencies to infra/opensearch

**Files:**
- Modify: `infra/opensearch/build.gradle.kts`

**Step 1: Add test dependencies**

Add to dependencies block:
```kotlin
testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
testImplementation("io.kotest:kotest-assertions-core:5.9.1")
testImplementation("io.mockk:mockk:1.13.13")
testImplementation("org.springframework.boot:spring-boot-starter-test")
```

**Step 2: Verify**

Run: `./gradlew :infra:opensearch:dependencies --configuration testCompileClasspath | grep kotest`
Expected: See kotest-runner-junit5:5.9.1 and kotest-assertions-core:5.9.1

**Step 3: Commit**

```bash
git add infra/opensearch/build.gradle.kts
git commit -m "test: add Kotest test dependencies to opensearch module"
```

---

## Task 1: Add commentCount to PostDocument + OpenSearch mapping

**Files:**
- Modify: `infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/document/PostDocument.kt`
- Create: `infra/opensearch/src/test/kotlin/com/anoncommunity/infra/opensearch/document/PostDocumentSpec.kt`

**Step 1: Write the failing test**

Create `PostDocumentSpec.kt`:
```kotlin
package com.anoncommunity.infra.opensearch.document

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class PostDocumentSpec : DescribeSpec({

    fun createTestPost(): Post = Post.create(
        id = PostId(1L),
        authorId = com.anoncommunity.contract.model.MemberId(100L),
        title = Title("Test Post"),
        content = Content("Test content"),
        category = Category.DAILY,
        tags = emptyList(),
    )

    describe("PostDocument.fromModel") {
        it("should map commentCount from Post") {
            val post = createTestPost()
            val postWithStats = PostWithStats(post, PostStats(viewCount = 10, likeCount = 5, commentCount = 3))

            val doc = PostDocument.fromModel(postWithStats)

            doc.commentCount shouldBe 3
        }

        it("should default commentCount to 0 when stats is null") {
            val post = createTestPost()

            val doc = PostDocument.fromModel(post)

            doc.commentCount shouldBe 0
        }
    }

    describe("PostDocument.toModel") {
        it("should preserve commentCount in stats") {
            val doc = PostDocument(
                id = 1L,
                authorId = 100L,
                title = "Test",
                content = "Content",
                category = "DAILY",
                tags = emptyList(),
                status = "PUBLISHED",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                commentCount = 7,
            )

            val postWithStats = doc.toModel()

            postWithStats.stats?.commentCount shouldBe 7
        }
    }
})
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :infra:opensearch:test --tests PostDocumentSpec`
Expected: FAIL with "Unresolved reference: commentCount"

**Step 3: Add commentCount field to PostDocument**

Modify `PostDocument.kt`:
```kotlin
@Document(indexName = "posts")
data class PostDocument(
    @Id
    val id: Long,
    @Field(type = FieldType.Long)
    val authorId: Long,
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    val title: String,
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    val content: String,
    @Field(type = FieldType.Keyword)
    val category: String = "DAILY",
    @Field(type = FieldType.Keyword)
    val tags: List<String>,
    @Field(type = FieldType.Keyword)
    val status: String,
    @Field(type = FieldType.Date, format = [DateFormat.date_time])
    val createdAt: Instant,
    @Field(type = FieldType.Date, format = [DateFormat.date_time])
    val updatedAt: Instant,
    @Field(type = FieldType.Integer)
    val commentCount: Int = 0,
) {
    companion object {
        fun fromModel(post: Post): PostDocument {
            return PostDocument(
                id = post.id.value,
                authorId = post.authorId.value,
                title = post.title.value,
                content = post.content.value,
                category = post.category.name,
                tags = post.tags.map { it.value },
                status = post.status.name,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                commentCount = 0,
            )
        }

        fun fromModel(postWithStats: PostWithStats): PostDocument {
            val post = postWithStats.post
            return PostDocument(
                id = post.id.value,
                authorId = post.authorId.value,
                title = post.title.value,
                content = post.content.value,
                category = post.category.name,
                tags = post.tags.map { it.value },
                status = post.status.name,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                commentCount = postWithStats.stats?.commentCount ?: 0,
            )
        }
    }

    fun toModel(): PostWithStats {
        val post = Post.reconstruct(
            id = PostId(id),
            authorId = com.anoncommunity.contract.model.MemberId(authorId),
            title = Title(title),
            content = Content(content),
            category = Category.valueOf(category),
            tags = tags.map { Tag(it) },
            status = PostStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
        val stats = PostStats(
            viewCount = 0,  // Stats are enriched separately
            likeCount = 0,
            commentCount = commentCount,
        )
        return PostWithStats(post, stats)
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :infra:opensearch:test --tests PostDocumentSpec`
Expected: PASS (2 tests)

**Step 5: Commit**

```bash
git add infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/document/PostDocument.kt
git add infra/opensearch/src/test/kotlin/com/anoncommunity/infra/opensearch/document/PostDocumentSpec.kt
git commit -m "feat: add commentCount field to PostDocument for sorting support"
```

---

## Task 2: Implement PostIndexWriter.updateCommentCount

**Files:**
- Modify: `infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/PostIndexWriter.kt`
- Create: `infra/opensearch/src/test/kotlin/com/anoncommunity/infra/opensearch/PostIndexWriterSpec.kt`

**Step 1: Write the failing test**

Create `PostIndexWriterSpec.kt`:
```kotlin
package com.anoncommunity.infra.opensearch

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.infra.opensearch.document.PostDocument
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Result
import org.opensearch.client.opensearch.core.UpdateRequest
import org.opensearch.client.opensearch.core.UpdateResponse
import java.time.Instant

class PostIndexWriterSpec : DescribeSpec({

    lateinit var openSearchClient: OpenSearchClient
    lateinit var postIndexWriter: PostIndexWriter

    beforeEach {
        openSearchClient = mockk()
        postIndexWriter = PostIndexWriter(openSearchClient)
    }

    describe("PostIndexWriter.updateCommentCount") {
        it("should increment commentCount by delta using update API") {
            val postId = PostId(123L)
            val delta = 1
            val updateResponse = mockk<UpdateResponse<PostDocument>>()

            every { updateResponse.result() } returns Result.Updated
            every { openSearchClient.update(any<UpdateRequest<PostDocument, Map<String, Any>>>(), PostDocument::class.java) } returns updateResponse

            postIndexWriter.updateCommentCount(postId, delta)

            verify {
                openSearchClient.update(
                    withArg<UpdateRequest<PostDocument, Map<String, Any>>> { req ->
                        req.index() shouldBe "posts"
                        req.id() shouldBe "123"
                        val script = req.script()
                        script.inline()?.source() shouldBe "ctx._source.commentCount += params.delta"
                        script.inline()?.params()?.get("delta") shouldBe 1
                    },
                    PostDocument::class.java
                )
            }
        }

        it("should decrement commentCount by negative delta") {
            val postId = PostId(456L)
            val delta = -1
            val updateResponse = mockk<UpdateResponse<PostDocument>>()

            every { updateResponse.result() } returns Result.Updated
            every { openSearchClient.update(any<UpdateRequest<PostDocument, Map<String, Any>>>(), PostDocument::class.java) } returns updateResponse

            postIndexWriter.updateCommentCount(postId, delta)

            verify {
                openSearchClient.update(
                    withArg<UpdateRequest<PostDocument, Map<String, Any>>> { req ->
                        req.id() shouldBe "456"
                        val script = req.script()
                        script.inline()?.params()?.get("delta") shouldBe -1
                    },
                    PostDocument::class.java
                )
            }
        }

        it("should not throw when update fails (eventual consistency tolerated)") {
            val postId = PostId(789L)
            every { openSearchClient.update(any<UpdateRequest<PostDocument, Map<String, Any>>>(), PostDocument::class.java) } throws RuntimeException("Connection timeout")

            // Should not throw
            postIndexWriter.updateCommentCount(postId, 1)
        }
    }
})
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :infra:opensearch:test --tests PostIndexWriterSpec`
Expected: FAIL with "Unresolved reference: updateCommentCount"

**Step 3: Implement PostIndexWriter.updateCommentCount**

Modify `PostIndexWriter.kt`:
```kotlin
package com.anoncommunity.infra.opensearch

import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostWithStats
import com.anoncommunity.infra.opensearch.document.PostDocument
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Script
import org.opensearch.client.opensearch.core.UpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PostIndexWriter(
    private val openSearchClient: OpenSearchClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createOrUpdate(post: Post) {
        try {
            val document = PostDocument.fromModel(post)
            openSearchClient.index { req ->
                req.index("posts")
                    .id(document.id.toString())
                    .document(document)
            }
            logger.debug("Indexed post: ${post.id.value}")
        } catch (e: Exception) {
            logger.error("Failed to index post: ${post.id.value}", e)
            throw e
        }
    }

    fun createOrUpdate(postWithStats: PostWithStats) {
        try {
            val document = PostDocument.fromModel(postWithStats)
            openSearchClient.index { req ->
                req.index("posts")
                    .id(document.id.toString())
                    .document(document)
            }
            logger.debug("Indexed post with stats: ${postWithStats.post.id.value}")
        } catch (e: Exception) {
            logger.error("Failed to index post with stats: ${postWithStats.post.id.value}", e)
            throw e
        }
    }

    fun delete(postId: PostId) {
        try {
            openSearchClient.delete { req ->
                req.index("posts")
                    .id(postId.value.toString())
            }
            logger.debug("Deleted post from index: ${postId.value}")
        } catch (e: Exception) {
            logger.error("Failed to delete post from index: ${postId.value}", e)
            throw e
        }
    }

    fun updateCommentCount(postId: PostId, delta: Int) {
        try {
            val script = Script.of { s ->
                s.inline { i ->
                    i.source("ctx._source.commentCount += params.delta")
                        .params("delta", delta)
                }
            }

            openSearchClient.update(
                UpdateRequest.Builder<PostDocument, Map<String, Any>>()
                    .index("posts")
                    .id(postId.value.toString())
                    .script(script)
                    .build(),
                PostDocument::class.java
            )
            logger.debug("Updated commentCount for post ${postId.value} by delta=$delta")
        } catch (e: Exception) {
            // Tolerate failures - eventual consistency model
            logger.warn("Failed to update commentCount for post ${postId.value}, will retry on next event", e)
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :infra:opensearch:test --tests PostIndexWriterSpec`
Expected: PASS (3 tests)

**Step 5: Commit**

```bash
git add infra/opensearch/src/main/kotlin/com/anoncommunity/infra/opensearch/PostIndexWriter.kt
git add infra/opensearch/src/test/kotlin/com/anoncommunity/infra/opensearch/PostIndexWriterSpec.kt
git commit -m "feat: implement PostIndexWriter.updateCommentCount for CDC sync"
```

---

## Task 3: Update CommentEventConsumer to sync OpenSearch

**Files:**
- Modify: `app/consumer/src/main/kotlin/com/anoncommunity/app/consumer/domain/CommentEventConsumer.kt`
- Modify: `app/consumer/src/test/kotlin/com/anoncommunity/app/consumer/domain/CommentEventConsumerSpec.kt`

**Step 1: Write the failing test**

Modify `CommentEventConsumerSpec.kt` to add new test cases:
```kotlin
// Add to existing describe blocks:

describe("CommentEventConsumer.handleCommentCreated") {
    // ... existing tests ...

    it("should increment OpenSearch commentCount when comment created") {
        val event = CommentCreated(
            commentId = CommentId(10L),
            postId = PostId(1L),
            authorId = MemberId(2L),
            content = CommentContent("Test comment"),
            depth = 0,
            parentCommentId = null,
            occurredAt = Instant.now(),
        )
        val comment = mockk<Comment>()
        every { commentRepository.findById(CommentId(10L)) } returns comment
        every { comment.isDeleted } returns false
        every { processedEventRepository.existsByEventId(event.eventId) } returns false
        every { processedEventRepository.save(any()) } just Runs
        every { postIndexWriter.updateCommentCount(PostId(1L), 1) } just Runs

        consumer.handleCommentCreated(event)

        verify { postIndexWriter.updateCommentCount(PostId(1L), 1) }
    }

    it("should skip OpenSearch update when event already processed") {
        val event = CommentCreated(
            commentId = CommentId(10L),
            postId = PostId(1L),
            authorId = MemberId(2L),
            content = CommentContent("Test comment"),
            depth = 0,
            parentCommentId = null,
            occurredAt = Instant.now(),
        )
        every { processedEventRepository.existsByEventId(event.eventId) } returns true

        consumer.handleCommentCreated(event)

        verify(exactly = 0) { postIndexWriter.updateCommentCount(any(), any()) }
    }
}

describe("CommentEventConsumer.handleCommentDeleted") {
    // ... existing tests ...

    it("should decrement OpenSearch commentCount when comment deleted") {
        val event = CommentDeleted(
            commentId = CommentId(20L),
            postId = PostId(2L),
            occurredAt = Instant.now(),
        )
        every { processedEventRepository.existsByEventId(event.eventId) } returns false
        every { processedEventRepository.save(any()) } just Runs
        every { postIndexWriter.updateCommentCount(PostId(2L), -1) } just Runs

        consumer.handleCommentDeleted(event)

        verify { postIndexWriter.updateCommentCount(PostId(2L), -1) }
    }

    it("should skip OpenSearch update when event already processed") {
        val event = CommentDeleted(
            commentId = CommentId(20L),
            postId = PostId(2L),
            occurredAt = Instant.now(),
        )
        every { processedEventRepository.existsByEventId(event.eventId) } returns true

        consumer.handleCommentDeleted(event)

        verify(exactly = 0) { postIndexWriter.updateCommentCount(any(), any()) }
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :app:consumer:test --tests CommentEventConsumerSpec`
Expected: FAIL with "Too few calls" (postIndexWriter.updateCommentCount not called)

**Step 3: Add OpenSearch sync logic to CommentEventConsumer**

Modify `CommentEventConsumer.kt`:
```kotlin
package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.CommentCreated
import com.anoncommunity.contract.event.CommentDeleted
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.port.outbound.CommentQueryPort
import com.anoncommunity.contract.port.outbound.ProcessedEventRepository
import com.anoncommunity.domain.consumer.ProcessedEvent
import com.anoncommunity.infra.opensearch.PostIndexWriter
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommentEventConsumer(
    private val commentRepository: CommentQueryPort,
    private val processedEventRepository: ProcessedEventRepository,
    private val postIndexWriter: PostIndexWriter,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["domain.comment.created"], groupId = "comment-consumer")
    @Transactional
    fun handleCommentCreated(event: CommentCreated) {
        if (processedEventRepository.existsByEventId(event.eventId)) {
            logger.debug("Event already processed: ${event.eventId}")
            return
        }

        val comment = commentRepository.findById(CommentId(event.commentId.value))
        if (comment == null) {
            logger.warn("Comment not found: ${event.commentId.value}")
            return
        }

        if (comment.isDeleted) {
            logger.debug("Comment already deleted: ${event.commentId.value}")
            return
        }

        // Sync to OpenSearch: increment commentCount
        postIndexWriter.updateCommentCount(event.postId, delta = 1)

        processedEventRepository.save(
            ProcessedEvent.create(
                eventId = event.eventId,
                eventType = "CommentCreated",
            )
        )
        logger.info("Processed CommentCreated event: ${event.eventId}, updated OpenSearch")
    }

    @KafkaListener(topics = ["domain.comment.deleted"], groupId = "comment-consumer")
    @Transactional
    fun handleCommentDeleted(event: CommentDeleted) {
        if (processedEventRepository.existsByEventId(event.eventId)) {
            logger.debug("Event already processed: ${event.eventId}")
            return
        }

        // Sync to OpenSearch: decrement commentCount
        postIndexWriter.updateCommentCount(event.postId, delta = -1)

        processedEventRepository.save(
            ProcessedEvent.create(
                eventId = event.eventId,
                eventType = "CommentDeleted",
            )
        )
        logger.info("Processed CommentDeleted event: ${event.eventId}, updated OpenSearch")
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :app:consumer:test --tests CommentEventConsumerSpec`
Expected: PASS (all tests including new ones)

**Step 5: Commit**

```bash
git add app/consumer/src/main/kotlin/com/anoncommunity/app/consumer/domain/CommentEventConsumer.kt
git add app/consumer/src/test/kotlin/com/anoncommunity/app/consumer/domain/CommentEventConsumerSpec.kt
git commit -m "feat: sync commentCount to OpenSearch on CommentCreated/Deleted events"
```

---

## Task 4: Add commentCount to FeedController ALLOWED_SORT_FIELDS

**Files:**
- Modify: `app/api/src/main/kotlin/com/anoncommunity/app/api/controller/FeedController.kt`
- Create: `app/api/src/test/kotlin/com/anoncommunity/app/api/controller/FeedControllerCommentSortSpec.kt`

**Step 1: Write the failing test**

Create `FeedControllerCommentSortSpec.kt`:
```kotlin
package com.anoncommunity.app.api.controller

import com.anoncommunity.app.usecase.feed.GetFeedUseCase
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FeedController::class)
class FeedControllerCommentSortSpec : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @MockkBean
    lateinit var getFeedUseCase: GetFeedUseCase

    @Autowired
    lateinit var mockMvc: MockMvc

    init {
        describe("GET /api/v1/feed with sortBy=commentCount") {
            it("should accept commentCount as valid sortBy parameter") {
                every { getFeedUseCase(any()) } returns mockk(relaxed = true)

                val result = mockMvc.perform(
                    get("/api/v1/feed")
                        .param("sortBy", "commentCount")
                        .param("sortDirection", "DESC")
                )

                result.andExpect(status().isOk)
                verify {
                    getFeedUseCase(
                        withArg { query ->
                            query.pageable.sortBy shouldBe "commentCount"
                        }
                    )
                }
            }

            it("should fallback to createdAt for invalid sortBy") {
                every { getFeedUseCase(any()) } returns mockk(relaxed = true)

                val result = mockMvc.perform(
                    get("/api/v1/feed")
                        .param("sortBy", "invalidField")
                        .param("sortDirection", "DESC")
                )

                result.andExpect(status().isOk)
                verify {
                    getFeedUseCase(
                        withArg { query ->
                            query.pageable.sortBy shouldBe "createdAt"
                        }
                    )
                }
            }
        }
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :app:api:test --tests FeedControllerCommentSortSpec`
Expected: FAIL - first test expects commentCount to be accepted but it's rejected (fallback to createdAt)

**Step 3: Add commentCount to ALLOWED_SORT_FIELDS**

Modify `FeedController.kt`:
```kotlin
companion object {
    private val ALLOWED_SORT_FIELDS = setOf("createdAt", "updatedAt", "commentCount")
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :app:api:test --tests FeedControllerCommentSortSpec`
Expected: PASS (2 tests)

**Step 5: Commit**

```bash
git add app/api/src/main/kotlin/com/anoncommunity/app/api/controller/FeedController.kt
git add app/api/src/test/kotlin/com/anoncommunity/app/api/controller/FeedControllerCommentSortSpec.kt
git commit -m "feat: add commentCount to ALLOWED_SORT_FIELDS for feed sorting"
```

---

## Task 5: Update frontend FeedSort type

**Files:**
- Modify: `frontend/src/lib/api/feed.ts`

**Step 1: Update FeedSort type definition**

Modify `feed.ts`:
```typescript
export type FeedSort = "latest" | "oldest" | "popular";
```

**Step 2: Verify TypeScript compilation**

Run: `cd frontend && npm run type-check`
Expected: No errors (FeedSort type updated successfully)

**Step 3: Commit**

```bash
git add frontend/src/lib/api/feed.ts
git commit -m "feat: extend FeedSort type to include 'popular' option"
```

---

## Task 6: Add feed API mapping for popular sort

**Files:**
- Modify: `frontend/src/lib/api/feed.ts`

**Step 1: Update feedApi.get to map 'popular' to commentCount**

Modify `feed.ts`:
```typescript
export const feedApi = {
  get: (page = 0, size = 20, category?: PostCategory, sort: FeedSort = "latest") => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    if (category) params.set("category", category);

    // Map frontend sort to backend sortBy/sortDirection
    if (sort === "popular") {
      params.set("sortBy", "commentCount");
      params.set("sortDirection", "DESC");
    } else {
      params.set("sortBy", "createdAt");
      params.set("sortDirection", sort === "latest" ? "DESC" : "ASC");
    }

    return fetchAPI<PageResponse<PostDetailResponse>>(`/feed?${params.toString()}`);
  },
};
```

**Step 2: Verify TypeScript compilation**

Run: `cd frontend && npm run type-check`
Expected: No errors

**Step 3: Commit**

```bash
git add frontend/src/lib/api/feed.ts
git commit -m "feat: map 'popular' sort to commentCount/DESC in feed API"
```

---

## Task 7: Add "댓글 많은 순" dropdown option

**Files:**
- Modify: `frontend/src/components/post/feed-list.tsx`

**Step 1: Add popular option to dropdown**

Modify `feed-list.tsx`:
```typescript
<select
  value={selectedSort}
  onChange={(e) => setSelectedSort(e.target.value as FeedSort)}
  className="shrink-0 rounded-md border bg-background px-2 py-1 text-xs text-muted-foreground"
>
  <option value="latest">최신순</option>
  <option value="popular">댓글 많은 순</option>
  <option value="oldest">오래된순</option>
</select>
```

**Step 2: Verify frontend build**

Run: `cd frontend && npm run build`
Expected: BUILD SUCCESS

**Step 3: Manual verification (optional)**

Run: `cd frontend && npm run dev`
Visit: http://localhost:3000
Expected: Dropdown shows 3 options: 최신순, 댓글 많은 순, 오래된순

**Step 4: Commit**

```bash
git add frontend/src/components/post/feed-list.tsx
git commit -m "feat: add '댓글 많은 순' dropdown option for comment count sorting"
```

---

## Task 8: Create initial data migration script

**Files:**
- Create: `scripts/sync-comment-counts.sh`

**Step 1: Write migration script**

Create `scripts/sync-comment-counts.sh`:
```bash
#!/bin/bash
set -euo pipefail

# Sync existing commentCounts from post_stats (RDB) to PostDocument (OpenSearch)
# Run once after deploying commentCount field to OpenSearch mapping

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
OPENSEARCH_URL="${OPENSEARCH_URL:-http://localhost:9200}"

echo "Starting commentCount sync from RDB to OpenSearch..."

# Fetch all post_stats with non-zero commentCount from RDB via backend API
# Assuming a temporary admin API endpoint exists: GET /admin/posts/stats
# Response format: [{"postId": 1, "commentCount": 5}, {"postId": 2, "commentCount": 3}, ...]

STATS_JSON=$(curl -s "${BACKEND_URL}/admin/posts/stats?minCommentCount=1")

if [ -z "$STATS_JSON" ] || [ "$STATS_JSON" = "[]" ]; then
    echo "No posts with comments found. Sync complete."
    exit 0
fi

echo "Found $(echo "$STATS_JSON" | jq '. | length') posts with comments to sync"

# Update each post document in OpenSearch
echo "$STATS_JSON" | jq -c '.[]' | while read -r stat; do
    POST_ID=$(echo "$stat" | jq -r '.postId')
    COMMENT_COUNT=$(echo "$stat" | jq -r '.commentCount')

    echo "Syncing postId=$POST_ID commentCount=$COMMENT_COUNT"

    curl -s -X POST "${OPENSEARCH_URL}/posts/_update/${POST_ID}" \
        -H 'Content-Type: application/json' \
        -d "{
            \"script\": {
                \"source\": \"ctx._source.commentCount = params.count\",
                \"params\": {
                    \"count\": ${COMMENT_COUNT}
                }
            }
        }" > /dev/null
done

echo "CommentCount sync completed successfully!"
```

**Step 2: Make script executable**

Run: `chmod +x scripts/sync-comment-counts.sh`

**Step 3: Add usage documentation to script**

Add header comment to `sync-comment-counts.sh`:
```bash
#!/bin/bash
# sync-comment-counts.sh
#
# Purpose: One-time migration to sync commentCount from RDB (post_stats) to OpenSearch (PostDocument)
#
# Prerequisites:
#   1. Deploy backend with commentCount field added to PostDocument mapping
#   2. Create temporary admin endpoint: GET /admin/posts/stats?minCommentCount=1
#      Response: [{"postId": Long, "commentCount": Int}, ...]
#   3. Ensure OpenSearch is accessible
#
# Usage:
#   BACKEND_URL=https://api.example.com OPENSEARCH_URL=https://opensearch.example.com ./scripts/sync-comment-counts.sh
#
# Note: This script is safe to run multiple times (idempotent update)
```

**Step 4: Commit**

```bash
git add scripts/sync-comment-counts.sh
git commit -m "ops: add initial commentCount sync script for OpenSearch migration"
```

**Note:** This script requires a temporary admin API endpoint to be created. Implementation of `/admin/posts/stats` is out of scope for this plan but should be added before running the migration in production.

---

## Task 9: Final build verification

**Files:** N/A (verification only)

**Step 1: Run full backend test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests pass (including new PostDocumentSpec, PostIndexWriterSpec, CommentEventConsumerSpec, FeedControllerCommentSortSpec)

**Step 2: Verify test count increased**

Run: `./gradlew test --info 2>&1 | grep -E "tests completed"`
Expected: More tests than before (at least +6 from new specs)

**Step 3: Run frontend build**

Run: `cd frontend && npm run build`
Expected: BUILD SUCCESS

**Step 4: Smoke test backend**

Run: `./gradlew :app:api:bootRun`
Wait for startup, then test endpoint:
```bash
curl "http://localhost:8080/api/v1/feed?sortBy=commentCount&sortDirection=DESC"
```
Expected: 200 OK response (may be empty if no data, but should not 400)

**Step 5: Smoke test frontend**

Run: `cd frontend && npm run dev`
Visit: http://localhost:3000
Expected:
- Dropdown shows 3 options: 최신순, 댓글 많은 순, 오래된순
- Selecting "댓글 많은 순" triggers API call with sortBy=commentCount&sortDirection=DESC

**Step 6: No commit needed (verification only)**

---

## Summary

| Task | Files | Covers |
|------|:---:|---|
| 0 | 1 modified | Test dependencies for opensearch module |
| 1 | 1 modified, 1 new | PostDocument.commentCount field + tests |
| 2 | 1 modified, 1 new | PostIndexWriter.updateCommentCount + tests |
| 3 | 2 modified | CommentEventConsumer OpenSearch sync + tests |
| 4 | 1 modified, 1 new | FeedController ALLOWED_SORT_FIELDS + tests |
| 5 | 1 modified | Frontend FeedSort type extension |
| 6 | 1 modified | Frontend feed API mapping for popular sort |
| 7 | 1 modified | Frontend dropdown UI with 댓글 많은 순 option |
| 8 | 1 new | Initial data migration script |
| 9 | N/A | Full build + smoke test verification |
| **Total** | **9 tasks, 13 file changes** | **Backend + Frontend + Migration** |

**Build dependencies changed:** 1 module
- `infra/opensearch`: +kotest-runner, +kotest-assertions, +mockk, +spring-boot-starter-test

**Architecture impact:**
- OpenSearch index schema changed (added commentCount field) - requires reindexing or migration
- CDC event handling enhanced (CommentCreated/Deleted → OpenSearch sync)
- Feed sorting extended (3 options: latest, popular, oldest)
- Eventual consistency model (~1 second lag for commentCount updates)

**Deployment notes:**
1. Deploy backend with new OpenSearch mapping
2. Run migration script to sync existing commentCounts
3. Deploy frontend with new dropdown option
4. Monitor CDC consumer lag and OpenSearch update latency

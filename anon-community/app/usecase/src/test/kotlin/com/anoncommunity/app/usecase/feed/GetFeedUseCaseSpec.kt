package com.anoncommunity.app.usecase.feed

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStats
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.port.PostFeedPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import com.anoncommunity.app.usecase.post.PostWithStats
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetFeedUseCaseSpec : DescribeSpec({

    val postFeedPort = mockk<PostFeedPort>()
    val postStatsQueryPort = mockk<PostStatsQueryPort>()

    val useCase = GetFeedUseCase(postFeedPort, postStatsQueryPort)

    val testPostId = PostId(1L)
    val testPost = Post.create(
        id = testPostId,
        authorProfileId = ProfileId(100L),
        title = PostTitle("Feed Post"),
        content = PostContent("Feed Content"),
    )

    val testStats = PostStats(
        postId = testPostId,
        likeCount = 5,
        commentCount = 3,
        viewCount = 100,
    )

    beforeEach {
        clearMocks(postFeedPort, postStatsQueryPort)
    }

    describe("GetFeedUseCase") {
        it("should delegate to postFeedPort and return result with stats") {
            val pageable = DomainPageable(page = 0, size = 10)
            val feedResult = PageResult(
                content = listOf(testPost),
                page = 0,
                size = 10,
                totalElements = 1L,
                totalPages = 1,
            )
            every { postFeedPort.getFeed(null, pageable) } returns feedResult
            every { postStatsQueryPort.findByPostIds(listOf(testPostId)) } returns mapOf(testPostId to testStats)

            val result = useCase(GetFeedQuery(pageable = pageable))

            result.content.size shouldBe 1
            result.content.first().post.id shouldBe testPostId
            result.content.first().stats shouldBe testStats
            result.content.first().stats!!.likeCount shouldBe 5
            result.content.first().stats!!.commentCount shouldBe 3
            result.content.first().stats!!.viewCount shouldBe 100
            result.page shouldBe 0
            result.size shouldBe 10
            result.totalElements shouldBe 1L
            result.totalPages shouldBe 1
            verify(exactly = 1) { postFeedPort.getFeed(null, pageable) }
            verify(exactly = 1) { postStatsQueryPort.findByPostIds(listOf(testPostId)) }
        }

        it("should return null stats when stats not found for a post") {
            val pageable = DomainPageable(page = 0, size = 10)
            val feedResult = PageResult(
                content = listOf(testPost),
                page = 0,
                size = 10,
                totalElements = 1L,
                totalPages = 1,
            )
            every { postFeedPort.getFeed(null, pageable) } returns feedResult
            every { postStatsQueryPort.findByPostIds(listOf(testPostId)) } returns emptyMap()

            val result = useCase(GetFeedQuery(pageable = pageable))

            result.content.size shouldBe 1
            result.content.first().post.id shouldBe testPostId
            result.content.first().stats shouldBe null
            verify(exactly = 1) { postFeedPort.getFeed(null, pageable) }
            verify(exactly = 1) { postStatsQueryPort.findByPostIds(listOf(testPostId)) }
        }
    }
})

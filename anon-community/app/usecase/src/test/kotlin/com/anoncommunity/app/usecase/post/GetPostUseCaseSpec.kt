package com.anoncommunity.app.usecase.post

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostStats
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.domain.post.port.PostQueryPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class GetPostUseCaseSpec : DescribeSpec({

    val postQueryPort = mockk<PostQueryPort>()
    val postStatsQueryPort = mockk<PostStatsQueryPort>()

    val useCase = GetPostUseCase(postQueryPort, postStatsQueryPort)

    val authorId = MemberId(100L)
    val postId = PostId(10L)

    val testPost = Post.create(
        id = postId,
        authorId = authorId,
        title = PostTitle("Test Post"),
        content = PostContent("Test Content"),
    )

    val testStats = PostStats(
        postId = postId,
        likeCount = 5,
        commentCount = 3,
        viewCount = 100,
    )

    beforeEach {
        clearMocks(postQueryPort, postStatsQueryPort)
    }

    describe("GetPostUseCase") {
        it("should return PostWithStats when both exist") {
            every { postQueryPort.findById(postId) } returns testPost
            every { postStatsQueryPort.findByPostId(postId) } returns testStats

            val result = useCase(GetPostQuery(postId = postId.value))

            result.post shouldBe testPost
            result.stats shouldBe testStats
            result.stats!!.likeCount shouldBe 5
            result.stats!!.commentCount shouldBe 3
            result.stats!!.viewCount shouldBe 100
        }

        it("should return PostWithStats with null stats") {
            every { postQueryPort.findById(postId) } returns testPost
            every { postStatsQueryPort.findByPostId(postId) } returns null

            val result = useCase(GetPostQuery(postId = postId.value))

            result.post shouldBe testPost
            result.stats shouldBe null
        }

        it("should throw PostNotFoundException when post does not exist") {
            every { postQueryPort.findById(any()) } returns null

            val exception = shouldThrow<PostNotFoundException> {
                useCase(GetPostQuery(postId = 999L))
            }
            exception.postId shouldBe PostId(999L)
        }

        context("post is not PUBLISHED") {
            fun nonPublishedPost(configure: Post.() -> Unit) = Post.create(
                id = postId, authorId = authorId,
                title = PostTitle("Test Post"), content = PostContent("Test Content"),
            ).also { it.pullDomainEvents(); it.configure() }

            listOf(
                "HIDDEN" to { nonPublishedPost { hide() } },
                "UNDER_REVIEW" to { nonPublishedPost { markForReview() } },
                "REMOVED" to { nonPublishedPost { remove() } },
                "DELETED" to { nonPublishedPost { softDelete() } },
            ).forEach { (label, buildPost) ->
                it("should throw PostNotFoundException when post is $label") {
                    every { postQueryPort.findById(postId) } returns buildPost()

                    val exception = shouldThrow<PostNotFoundException> {
                        useCase(GetPostQuery(postId = postId.value))
                    }
                    exception.postId shouldBe postId
                }
            }
        }
    }
})

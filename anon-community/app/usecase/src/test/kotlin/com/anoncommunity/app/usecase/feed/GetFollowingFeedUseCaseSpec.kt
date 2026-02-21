package com.anoncommunity.app.usecase.feed

import com.anoncommunity.contract.model.DomainPageable
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.FollowingIdsCache
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.port.PostFeedPort
import com.anoncommunity.domain.post.port.PostStatsQueryPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetFollowingFeedUseCaseSpec : DescribeSpec({
    val followQueryPort = mockk<FollowQueryPort>()
    val postFeedPort = mockk<PostFeedPort>()
    val postStatsQueryPort = mockk<PostStatsQueryPort>()
    val followingIdsCache = mockk<FollowingIdsCache>(relaxed = true)
    val useCase = GetFollowingFeedUseCase(followQueryPort, postFeedPort, postStatsQueryPort, followingIdsCache)

    beforeEach { clearMocks(followQueryPort, postFeedPort, postStatsQueryPort, followingIdsCache) }

    describe("GetFollowingFeedUseCase") {
        context("팔로잉이 없는 경우") {
            it("should return empty result") {
                every { followingIdsCache.get(ProfileId(1L)) } returns null
                every { followQueryPort.findFollowingIds(ProfileId(1L), 1000) } returns emptyList()

                val result = useCase.invoke(GetFollowingFeedQuery(activeProfileId = 1L, pageable = DomainPageable()))

                result.content.shouldBeEmpty()
                verify(exactly = 1) { followingIdsCache.set(ProfileId(1L), emptyList()) }
                verify(exactly = 0) { postFeedPort.getFollowingFeed(any(), any()) }
            }
        }

        context("캐시 미스 후 DB 조회") {
            it("should return posts and cache followingIds") {
                every { followingIdsCache.get(ProfileId(1L)) } returns null
                every { followQueryPort.findFollowingIds(ProfileId(1L), 1000) } returns listOf(ProfileId(2L), ProfileId(3L))
                val mockPost = mockk<Post>(relaxed = true)
                every { postFeedPort.getFollowingFeed(listOf(2L, 3L), any()) } returns PageResult(listOf(mockPost), 0, 20, 1L, 1)
                every { postStatsQueryPort.findByPostIds(any()) } returns emptyMap()

                val result = useCase.invoke(GetFollowingFeedQuery(activeProfileId = 1L, pageable = DomainPageable()))

                result.content.size shouldBe 1
                verify(exactly = 1) { followingIdsCache.set(ProfileId(1L), listOf(2L, 3L)) }
            }
        }

        context("캐시 히트") {
            it("should use cached followingIds and skip DB query") {
                every { followingIdsCache.get(ProfileId(1L)) } returns listOf(2L, 3L)
                val mockPost = mockk<Post>(relaxed = true)
                every { postFeedPort.getFollowingFeed(listOf(2L, 3L), any()) } returns PageResult(listOf(mockPost), 0, 20, 1L, 1)
                every { postStatsQueryPort.findByPostIds(any()) } returns emptyMap()

                useCase.invoke(GetFollowingFeedQuery(activeProfileId = 1L, pageable = DomainPageable()))

                verify(exactly = 0) { followQueryPort.findFollowingIds(any(), any()) }
            }
        }
    }
})

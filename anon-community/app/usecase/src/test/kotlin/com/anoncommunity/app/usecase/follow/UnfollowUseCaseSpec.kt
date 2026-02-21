package com.anoncommunity.app.usecase.follow

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.FollowCommandPort
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.FollowingIdsCache
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UnfollowUseCaseSpec : DescribeSpec({
    val followCommandPort = mockk<FollowCommandPort>(relaxed = true)
    val followQueryPort = mockk<FollowQueryPort>()
    val followingIdsCache = mockk<FollowingIdsCache>(relaxed = true)
    val useCase = UnfollowUseCase(followCommandPort, followQueryPort, followingIdsCache)

    beforeEach {
        clearMocks(followQueryPort)
        clearMocks(followCommandPort, followingIdsCache, answers = false)
    }

    describe("UnfollowUseCase") {
        context("팔로우 중인 경우") {
            it("should unfollow and invalidate cache") {
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns true

                useCase.invoke(UnfollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 1) { followCommandPort.delete(ProfileId(1L), ProfileId(2L)) }
                verify(exactly = 1) { followingIdsCache.invalidate(ProfileId(1L)) }
            }
        }

        context("팔로우하지 않은 경우") {
            it("should be idempotent — no delete called") {
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns false

                useCase.invoke(UnfollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 0) { followCommandPort.delete(any(), any()) }
            }
        }
    }
})

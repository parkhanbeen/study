package com.anoncommunity.app.usecase.follow

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.exception.ProfileNotFoundException
import com.anoncommunity.domain.member.port.FollowCommandPort
import com.anoncommunity.domain.member.port.FollowQueryPort
import com.anoncommunity.domain.member.port.FollowingIdsCache
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FollowUseCaseSpec : DescribeSpec({
    val followCommandPort = mockk<FollowCommandPort>(relaxed = true)
    val followQueryPort = mockk<FollowQueryPort>()
    val profileQueryPort = mockk<ProfileQueryPort>()
    val followingIdsCache = mockk<FollowingIdsCache>(relaxed = true)
    val useCase = FollowUseCase(followCommandPort, followQueryPort, profileQueryPort, followingIdsCache)

    beforeEach {
        clearMocks(followQueryPort, profileQueryPort)
        clearMocks(followCommandPort, followingIdsCache, answers = false)
    }

    describe("FollowUseCase") {
        context("정상 팔로우") {
            it("should follow a profile and invalidate cache") {
                every { profileQueryPort.findById(ProfileId(2L)) } returns mockk()
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns false

                useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 1) { followCommandPort.save(any()) }
                verify(exactly = 1) { followingIdsCache.invalidate(ProfileId(1L)) }
            }
        }

        context("자기 자신 팔로우") {
            it("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 1L))
                }
            }
        }

        context("이미 팔로우 중") {
            it("should be idempotent — no duplicate save") {
                every { profileQueryPort.findById(ProfileId(2L)) } returns mockk()
                every { followQueryPort.existsByFollowerAndFollowing(ProfileId(1L), ProfileId(2L)) } returns true

                useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 2L))

                verify(exactly = 0) { followCommandPort.save(any()) }
                verify(exactly = 0) { followingIdsCache.invalidate(any()) }
            }
        }

        context("존재하지 않는 프로필 팔로우") {
            it("should throw ProfileNotFoundException") {
                every { profileQueryPort.findById(ProfileId(99L)) } returns null

                shouldThrow<ProfileNotFoundException> {
                    useCase.invoke(FollowCommand(followerProfileId = 1L, followingProfileId = 99L))
                }
            }
        }
    }
})

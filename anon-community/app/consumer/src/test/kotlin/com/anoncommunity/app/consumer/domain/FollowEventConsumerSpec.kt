package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FollowEventConsumerSpec : DescribeSpec({
    val profileCommandPort = mockk<ProfileCommandPort>()
    val processedEventPort = mockk<ProcessedEventPort>()
    val objectMapper = jacksonObjectMapper()
    val consumer = FollowEventConsumer(profileCommandPort, processedEventPort, objectMapper)

    beforeEach { clearMocks(profileCommandPort, processedEventPort) }

    describe("FollowEventConsumer") {

        context("팔로우 (ExtractNewRecordState flattened: __op=c)") {
            it("should increment follower and following counts") {
                every { processedEventPort.markProcessed("1_c", "FollowCreated") } returns true
                every { profileCommandPort.incrementFollowerCount(ProfileId(2L)) } returns 1
                every { profileCommandPort.incrementFollowingCount(ProfileId(1L)) } returns 1

                val message = """{"id":1,"follower_profile_id":1,"following_profile_id":2,"__op":"c","__deleted":"false"}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.incrementFollowerCount(ProfileId(2L)) }
                verify(exactly = 1) { profileCommandPort.incrementFollowingCount(ProfileId(1L)) }
            }

            it("should skip when already processed (idempotency)") {
                every { processedEventPort.markProcessed("1_c", "FollowCreated") } returns false
                val message = """{"id":1,"follower_profile_id":1,"following_profile_id":2,"__op":"c","__deleted":"false"}"""
                consumer.consume(message)
                verify(exactly = 0) { profileCommandPort.incrementFollowerCount(any()) }
                verify(exactly = 0) { profileCommandPort.incrementFollowingCount(any()) }
            }
        }

        context("언팔로우 (ExtractNewRecordState flattened: __op=d, __deleted=true)") {
            it("should decrement follower and following counts") {
                every { processedEventPort.markProcessed("1_d", "FollowDeleted") } returns true
                every { profileCommandPort.decrementFollowerCount(ProfileId(2L)) } returns 1
                every { profileCommandPort.decrementFollowingCount(ProfileId(1L)) } returns 1

                val message = """{"id":1,"follower_profile_id":1,"following_profile_id":2,"__op":"d","__deleted":"true"}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.decrementFollowerCount(ProfileId(2L)) }
                verify(exactly = 1) { profileCommandPort.decrementFollowingCount(ProfileId(1L)) }
            }

            it("should skip when already processed (idempotency)") {
                every { processedEventPort.markProcessed("1_d", "FollowDeleted") } returns false
                val message = """{"id":1,"follower_profile_id":1,"following_profile_id":2,"__op":"d","__deleted":"true"}"""
                consumer.consume(message)
                verify(exactly = 0) { profileCommandPort.decrementFollowerCount(any()) }
                verify(exactly = 0) { profileCommandPort.decrementFollowingCount(any()) }
            }
        }

        context("tombstone (null message)") {
            it("should return without any processing") {
                consumer.consume(null)
                verify(exactly = 0) { profileCommandPort.incrementFollowerCount(any()) }
                verify(exactly = 0) { profileCommandPort.decrementFollowerCount(any()) }
            }
        }

        context("snapshot read (__op=r)") {
            it("should not call any count method") {
                val message = """{"id":1,"follower_profile_id":1,"following_profile_id":2,"__op":"r","__deleted":"false"}"""
                consumer.consume(message)
                verify(exactly = 0) { profileCommandPort.incrementFollowerCount(any()) }
                verify(exactly = 0) { profileCommandPort.decrementFollowerCount(any()) }
            }
        }
    }
})

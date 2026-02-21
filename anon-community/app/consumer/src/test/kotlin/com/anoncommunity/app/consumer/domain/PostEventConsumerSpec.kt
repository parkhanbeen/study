package com.anoncommunity.app.consumer.domain

import com.anoncommunity.contract.event.ProcessedEventPort
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.Bio
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.member.port.ProfileCommandPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class PostEventConsumerSpec : DescribeSpec({

    val profileQueryPort = mockk<ProfileQueryPort>()
    val profileCommandPort = mockk<ProfileCommandPort>(relaxed = true)
    val processedEventPort = mockk<ProcessedEventPort>()
    val objectMapper: ObjectMapper = jacksonObjectMapper()
    val consumer = PostEventConsumer(profileQueryPort, profileCommandPort, processedEventPort, objectMapper)

    beforeEach {
        clearMocks(profileQueryPort, profileCommandPort, processedEventPort)
    }

    describe("PostEventConsumer") {

        context("PostCreated event") {

            it("should increment profile postCount when event is newly processed") {
                val profile = MemberProfile.create(
                    id = ProfileId(100L),
                    memberId = MemberId(1L),
                    nickname = MemberNickname("testuser"),
                    bio = Bio("hello"),
                )
                every { processedEventPort.markProcessed("evt-1", "PostCreated") } returns true
                every { profileQueryPort.findById(ProfileId(100L)) } returns profile

                val message = """{"eventId":"evt-1","eventType":"PostCreated","authorProfileId":{"value":100}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.save(profile) }
                profile.postCount shouldBe 1
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-1", "PostCreated") } returns false

                val message = """{"eventId":"evt-1","eventType":"PostCreated","authorProfileId":{"value":100}}"""
                consumer.consume(message)

                verify(exactly = 0) { profileQueryPort.findById(any()) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }

            it("should skip save when profile is not found") {
                every { processedEventPort.markProcessed("evt-2", "PostCreated") } returns true
                every { profileQueryPort.findById(ProfileId(999L)) } returns null

                val message = """{"eventId":"evt-2","eventType":"PostCreated","authorProfileId":{"value":999}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileQueryPort.findById(ProfileId(999L)) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }

        context("PostDeleted event") {

            it("should decrement profile postCount when event is newly processed") {
                val profile = MemberProfile.create(
                    id = ProfileId(100L),
                    memberId = MemberId(1L),
                    nickname = MemberNickname("testuser"),
                    bio = Bio("hello"),
                )
                profile.incrementPostCount() // postCount = 1, so decrement is safe
                every { processedEventPort.markProcessed("evt-3", "PostDeleted") } returns true
                every { profileQueryPort.findById(ProfileId(100L)) } returns profile

                val message = """{"eventId":"evt-3","eventType":"PostDeleted","authorProfileId":{"value":100}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileCommandPort.save(profile) }
                profile.postCount shouldBe 0
            }

            it("should skip processing when event is already processed (idempotency)") {
                every { processedEventPort.markProcessed("evt-4", "PostDeleted") } returns false

                val message = """{"eventId":"evt-4","eventType":"PostDeleted","authorProfileId":{"value":100}}"""
                consumer.consume(message)

                verify(exactly = 0) { profileQueryPort.findById(any()) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }

            it("should skip save when profile is not found") {
                every { processedEventPort.markProcessed("evt-5", "PostDeleted") } returns true
                every { profileQueryPort.findById(ProfileId(200L)) } returns null

                val message = """{"eventId":"evt-5","eventType":"PostDeleted","authorProfileId":{"value":200}}"""
                consumer.consume(message)

                verify(exactly = 1) { profileQueryPort.findById(ProfileId(200L)) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }

        context("unknown event type") {

            it("should not interact with profile ports for unknown event types") {
                every { processedEventPort.markProcessed("evt-6", "PostArchived") } returns true

                val message = """{"eventId":"evt-6","eventType":"PostArchived","authorProfileId":{"value":100}}"""
                consumer.consume(message)

                verify(exactly = 0) { profileQueryPort.findById(any()) }
                verify(exactly = 0) { profileCommandPort.save(any()) }
            }
        }
    }
})

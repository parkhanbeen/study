package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.PreferredLanguage
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.domain.member.port.MemberQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UpdateLanguageUseCaseSpec : DescribeSpec({

    val memberQueryPort = mockk<MemberQueryPort>()
    val memberCommandPort = mockk<MemberCommandPort>()

    val useCase = UpdateLanguageUseCase(memberQueryPort, memberCommandPort)

    beforeEach {
        clearMocks(memberQueryPort, memberCommandPort)
    }

    describe("UpdateLanguageUseCase") {
        it("should update language for existing member") {
            val member = Member.create(
                id = MemberId(1L),
            )
            every { memberQueryPort.findById(MemberId(1L)) } returns member
            every { memberCommandPort.save(any()) } answers { firstArg() }

            useCase(UpdateLanguageCommand(memberId = 1L, language = "en"))

            member.preferredLanguage shouldBe PreferredLanguage("en")
            verify(exactly = 1) { memberCommandPort.save(member) }
        }

        it("should throw MemberNotFoundException when member not found") {
            every { memberQueryPort.findById(any()) } returns null

            val exception = shouldThrow<MemberNotFoundException> {
                useCase(UpdateLanguageCommand(memberId = 999L, language = "ko"))
            }
            exception.memberId shouldBe MemberId(999L)

            verify(exactly = 0) { memberCommandPort.save(any()) }
        }

        it("should throw IllegalArgumentException for unsupported language") {
            val member = Member.create(
                id = MemberId(1L),
            )
            every { memberQueryPort.findById(MemberId(1L)) } returns member

            shouldThrow<IllegalArgumentException> {
                useCase(UpdateLanguageCommand(memberId = 1L, language = "fr"))
            }

            verify(exactly = 0) { memberCommandPort.save(any()) }
        }
    }
})

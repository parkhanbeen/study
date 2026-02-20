package com.anoncommunity.app.usecase.search

import com.anoncommunity.domain.post.port.PostSearchPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetHotTagsUseCaseSpec : DescribeSpec({

    val postSearchPort = mockk<PostSearchPort>()
    val useCase = GetHotTagsUseCase(postSearchPort)

    beforeEach {
        clearMocks(postSearchPort)
    }

    describe("GetHotTagsUseCase") {

        context("when port returns tags") {
            it("should return tags from port") {
                val expectedTags = listOf("kotlin", "spring", "backend")
                every { postSearchPort.getHotTags(10) } returns expectedTags

                val result = useCase(10)

                result shouldBe expectedTags
                verify(exactly = 1) { postSearchPort.getHotTags(10) }
            }

            it("should use default limit of 10") {
                val expectedTags = listOf("kotlin", "spring")
                every { postSearchPort.getHotTags(10) } returns expectedTags

                val result = useCase()

                result shouldBe expectedTags
                verify(exactly = 1) { postSearchPort.getHotTags(10) }
            }

            it("should pass custom limit to port") {
                val expectedTags = listOf("kotlin", "spring", "jvm", "backend", "api")
                every { postSearchPort.getHotTags(5) } returns expectedTags

                val result = useCase(5)

                result shouldBe expectedTags
                verify(exactly = 1) { postSearchPort.getHotTags(5) }
            }
        }

        context("when port returns empty list") {
            it("should return empty list when port returns empty") {
                every { postSearchPort.getHotTags(10) } returns emptyList()

                val result = useCase()

                result shouldBe emptyList()
            }
        }
    }
})

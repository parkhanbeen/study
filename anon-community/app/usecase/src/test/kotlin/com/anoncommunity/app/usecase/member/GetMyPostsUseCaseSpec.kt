package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.port.PostQueryPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class GetMyPostsUseCaseSpec : DescribeSpec({

    val postQueryPort = mockk<PostQueryPort>()
    val useCase = GetMyPostsUseCase(postQueryPort)

    val memberId = MemberId(100L)

    beforeEach { clearMocks(postQueryPort) }

    describe("GetMyPostsUseCase") {
        it("should return page of posts authored by member") {
            val post = Post.create(
                id = PostId(1L),
                authorId = memberId,
                title = PostTitle("My Post"),
                content = PostContent("Content"),
            )
            val page = PageResult(content = listOf(post), page = 0, size = 20, totalElements = 1L, totalPages = 1)
            every { postQueryPort.findByAuthorId(memberId, any()) } returns page

            val result = useCase(GetMyPostsQuery(memberId = memberId.value))

            result.content.size shouldBe 1
            result.content[0].authorId shouldBe memberId
            result.totalElements shouldBe 1L
        }

        it("should return empty page when member has no posts") {
            val emptyPage = PageResult(content = emptyList<Post>(), page = 0, size = 20, totalElements = 0L, totalPages = 0)
            every { postQueryPort.findByAuthorId(memberId, any()) } returns emptyPage

            val result = useCase(GetMyPostsQuery(memberId = memberId.value))

            result.content shouldBe emptyList()
            result.totalElements shouldBe 0L
        }
    }
})

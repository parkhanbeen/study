package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.like.port.LikeQueryPort
import com.anoncommunity.domain.post.Post
import com.anoncommunity.domain.post.PostContent
import com.anoncommunity.domain.post.PostTitle
import com.anoncommunity.domain.post.port.PostQueryPort
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class GetMyLikedPostsUseCaseSpec : DescribeSpec({

    val likeQueryPort = mockk<LikeQueryPort>()
    val postQueryPort = mockk<PostQueryPort>()
    val useCase = GetMyLikedPostsUseCase(likeQueryPort, postQueryPort)

    val profileId = ProfileId(100L)
    val postId = PostId(10L)

    beforeEach { clearMocks(likeQueryPort, postQueryPort) }

    describe("GetMyLikedPostsUseCase") {
        it("should return posts that the member has liked") {
            val post = Post.create(
                id = postId,
                authorProfileId = ProfileId(1L),
                title = PostTitle("Liked Post"),
                content = PostContent("Content"),
            )
            val postIdsPage = PageResult(
                content = listOf(postId),
                page = 0,
                size = 20,
                totalElements = 1L,
                totalPages = 1,
            )

            every { likeQueryPort.findPostIdsByProfileId(profileId, any()) } returns postIdsPage
            every { postQueryPort.findById(postId) } returns post

            val result = useCase(GetMyLikedPostsQuery(memberId = profileId.value))

            result.content.size shouldBe 1
            result.totalElements shouldBe 1L
        }

        it("should return empty page when member has no liked posts") {
            val emptyPage = PageResult(
                content = emptyList<PostId>(),
                page = 0,
                size = 20,
                totalElements = 0L,
                totalPages = 0,
            )
            every { likeQueryPort.findPostIdsByProfileId(profileId, any()) } returns emptyPage

            val result = useCase(GetMyLikedPostsQuery(memberId = profileId.value))

            result.content shouldBe emptyList()
        }
    }
})

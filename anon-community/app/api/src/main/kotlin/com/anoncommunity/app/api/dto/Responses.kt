package com.anoncommunity.app.api.dto

import com.anoncommunity.contract.model.PageResult
import com.anoncommunity.domain.comment.Comment
import com.anoncommunity.domain.member.Member
import com.anoncommunity.domain.member.MemberProfile
import com.anoncommunity.domain.post.Post
import com.anoncommunity.app.usecase.post.PostWithStats
import com.anoncommunity.domain.post.port.PostSearchHit
import java.time.Instant

data class PostResponse(
    val id: Long,
    val authorProfileId: Long,
    val title: String,
    val content: String,
    val category: String,
    val tags: List<String>,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(post: Post) = PostResponse(
            id = post.id.value,
            authorProfileId = post.authorProfileId.value,
            title = post.title.value,
            content = post.content.value,
            category = post.category.name,
            tags = post.tags.map { it.value },
            status = post.status.name,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
        )
    }
}

data class PostDetailResponse(
    val id: Long,
    val authorProfileId: Long,
    val title: String,
    val content: String,
    val category: String,
    val tags: List<String>,
    val status: String,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(result: PostWithStats) = PostDetailResponse(
            id = result.post.id.value,
            authorProfileId = result.post.authorProfileId.value,
            title = result.post.title.value,
            content = result.post.content.value,
            category = result.post.category.name,
            tags = result.post.tags.map { it.value },
            status = result.post.status.name,
            likeCount = result.stats?.likeCount ?: 0,
            commentCount = result.stats?.commentCount ?: 0,
            viewCount = result.stats?.viewCount ?: 0,
            createdAt = result.post.createdAt,
            updatedAt = result.post.updatedAt,
        )
    }
}

data class PostSearchHitResponse(
    val id: Long,
    val authorProfileId: Long,
    val title: String,
    val content: String,
    val category: String,
    val tags: List<String>,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val highlights: Map<String, List<String>>,
) {
    companion object {
        fun from(hit: PostSearchHit) = PostSearchHitResponse(
            id = hit.post.id.value,
            authorProfileId = hit.post.authorProfileId.value,
            title = hit.post.title.value,
            content = hit.post.content.value,
            category = hit.post.category.name,
            tags = hit.post.tags.map { it.value },
            status = hit.post.status.name,
            createdAt = hit.post.createdAt,
            updatedAt = hit.post.updatedAt,
            highlights = hit.highlights,
        )
    }
}

data class CommentResponse(
    val id: Long,
    val postId: Long,
    val authorProfileId: Long,
    val content: String,
    val parentCommentId: Long?,
    val depth: Int,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(comment: Comment) = CommentResponse(
            id = comment.id.value,
            postId = comment.postId.value,
            authorProfileId = comment.authorProfileId.value,
            content = comment.content.value,
            parentCommentId = comment.parentCommentId?.value,
            depth = comment.depth,
            status = comment.status.name,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
        )
    }
}

data class MemberResponse(
    val id: Long,
    val nickname: String,
    val bio: String,
    val postCount: Int,
    val preferredLanguage: String?,
    val createdAt: Instant,
) {
    companion object {
        fun from(member: Member, profile: MemberProfile? = null) = MemberResponse(
            id = member.id.value,
            nickname = profile?.nickname?.value ?: "",
            bio = profile?.bio?.value ?: "",
            postCount = profile?.postCount ?: 0,
            preferredLanguage = member.preferredLanguage?.value,
            createdAt = member.createdAt,
        )
    }
}

data class MemberCreatedResponse(
    val member: MemberResponse,
    val accessToken: String,
    val refreshToken: String,
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun <T, R> from(result: PageResult<T>, mapper: (T) -> R) = PageResponse(
            content = result.content.map(mapper),
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}

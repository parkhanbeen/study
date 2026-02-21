package com.anoncommunity.app.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreatePostRequest(
    @field:NotBlank @field:Size(max = 200) val title: String,
    @field:NotBlank @field:Size(max = 10000) val content: String,
    @field:NotBlank val category: String,
    @field:Size(max = 20, message = "Tags cannot exceed 20") val tags: List<String> = emptyList(),
)

data class UpdatePostRequest(
    @field:Size(max = 200) val title: String? = null,
    @field:Size(max = 10000) val content: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
)

data class CreateCommentRequest(
    @field:NotBlank @field:Size(max = 1000) val content: String,
    val parentCommentId: Long? = null,
)

data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다")
    @field:Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다")
    val content: String,
)


data class CreateMemberRequest(
    @field:NotBlank @field:Size(min = 2, max = 20) val nickname: String,
    @field:NotBlank @field:Size(min = 8, max = 100) val password: String,
    @field:Size(max = 200) val bio: String? = null,
)

data class UpdateLanguageRequest(
    @field:Pattern(regexp = "^(ko|en|ja|zh)$", message = "Supported languages: ko, en, ja, zh")
    val language: String,
)

data class CreateProfileRequest(
    @field:NotBlank @field:Size(min = 2, max = 20) val nickname: String,
    @field:Size(max = 200) val bio: String? = null,
)

data class UpdateProfileRequest(
    @field:NotBlank @field:Size(min = 2, max = 20) val nickname: String,
    @field:Size(max = 200) val bio: String? = null,
)

data class SwitchProfileRequest(
    val profileId: Long,
)

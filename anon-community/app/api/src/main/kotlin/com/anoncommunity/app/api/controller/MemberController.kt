package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.dto.*
import com.anoncommunity.app.api.security.JwtProperties
import com.anoncommunity.app.api.security.JwtTokenProvider
import com.anoncommunity.app.usecase.member.CreateMemberCommand
import com.anoncommunity.contract.auth.RefreshTokenPort
import com.anoncommunity.app.usecase.member.CreateMemberUseCase
import com.anoncommunity.app.usecase.member.GetMyLikedPostsQuery
import com.anoncommunity.app.usecase.member.GetMyLikedPostsUseCase
import com.anoncommunity.app.usecase.member.GetMyPostsQuery
import com.anoncommunity.app.usecase.member.GetMyPostsUseCase
import com.anoncommunity.app.usecase.member.UpdateLanguageCommand
import com.anoncommunity.app.usecase.member.UpdateLanguageUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val createMemberUseCase: CreateMemberUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val getMyPostsUseCase: GetMyPostsUseCase,
    private val getMyLikedPostsUseCase: GetMyLikedPostsUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenPort: RefreshTokenPort,
) {

    @Operation(summary = "회원가입 (JWT 토큰 포함 응답)")
    @PostMapping
    fun createMember(@Valid @RequestBody request: CreateMemberRequest): ResponseEntity<MemberCreatedResponse> {
        val member = createMemberUseCase(CreateMemberCommand(
            nickname = request.nickname,
            password = request.password,
            bio = request.bio ?: "",
        ))

        val accessToken = jwtTokenProvider.issueAccessToken(member.id.value)
        val refreshToken = jwtTokenProvider.issueRefreshToken(member.id.value)
        refreshTokenPort.store(member.id.value, refreshToken, jwtProperties.refreshTokenExpiry)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            MemberCreatedResponse(
                member = MemberResponse.from(member),
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )
    }

    @Operation(summary = "언어 설정 변경")
    @PutMapping("/me/language")
    fun updateLanguage(
        @RequestHeader("X-Member-Id") memberId: Long,
        @Valid @RequestBody request: UpdateLanguageRequest,
    ): ResponseEntity<Unit> {
        updateLanguageUseCase(UpdateLanguageCommand(
            memberId = memberId,
            language = request.language,
        ))
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "내가 쓴 글 목록")
    @GetMapping("/me/posts")
    fun getMyPosts(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PageResponse<PostResponse>> {
        val result = getMyPostsUseCase(GetMyPostsQuery(memberId = memberId, page = page, size = size))
        return ResponseEntity.ok(PageResponse.from(result, PostResponse::from))
    }

    @Operation(summary = "내가 좋아요한 글 목록")
    @GetMapping("/me/liked-posts")
    fun getMyLikedPosts(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PageResponse<PostResponse>> {
        val result = getMyLikedPostsUseCase(GetMyLikedPostsQuery(memberId = memberId, page = page, size = size))
        return ResponseEntity.ok(PageResponse.from(result, PostResponse::from))
    }
}

package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.dto.*
import com.anoncommunity.app.api.security.JwtProperties
import com.anoncommunity.app.api.security.JwtTokenProvider
import com.anoncommunity.app.usecase.member.CreateMemberCommand
import com.anoncommunity.contract.auth.RefreshTokenPort
import com.anoncommunity.app.usecase.member.CreateMemberUseCase
import com.anoncommunity.app.usecase.member.CreateProfileCommand
import com.anoncommunity.app.usecase.member.CreateProfileUseCase
import com.anoncommunity.app.usecase.member.DeleteProfileCommand
import com.anoncommunity.app.usecase.member.DeleteProfileUseCase
import com.anoncommunity.app.usecase.member.GetMyLikedPostsQuery
import com.anoncommunity.app.usecase.member.GetMyLikedPostsUseCase
import com.anoncommunity.app.usecase.member.GetMyPostsQuery
import com.anoncommunity.app.usecase.member.GetMyPostsUseCase
import com.anoncommunity.app.usecase.member.GetMyProfilesQuery
import com.anoncommunity.app.usecase.member.GetMyProfilesUseCase
import com.anoncommunity.app.usecase.member.UpdateLanguageCommand
import com.anoncommunity.app.usecase.member.UpdateLanguageUseCase
import com.anoncommunity.app.usecase.member.UpdateProfileCommand
import com.anoncommunity.app.usecase.member.UpdateProfileUseCase
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
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val getMyProfilesUseCase: GetMyProfilesUseCase,
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

        // activeProfileId is 0L until Task 13 creates a default profile during signup
        val accessToken = jwtTokenProvider.issueAccessToken(member.id.value, activeProfileId = 0L)
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

    @Operation(summary = "내 프로필 목록 조회")
    @GetMapping("/me/profiles")
    fun getMyProfiles(
        @RequestHeader("X-Member-Id") memberId: Long,
    ): ResponseEntity<List<ProfileResponse>> {
        val profiles = getMyProfilesUseCase(GetMyProfilesQuery(memberId = memberId))
        return ResponseEntity.ok(profiles.map { ProfileResponse.from(it) })
    }

    @Operation(summary = "프로필 생성")
    @PostMapping("/me/profiles")
    fun createProfile(
        @RequestHeader("X-Member-Id") memberId: Long,
        @Valid @RequestBody request: CreateProfileRequest,
    ): ResponseEntity<ProfileResponse> {
        val profile = createProfileUseCase(
            CreateProfileCommand(
                memberId = memberId,
                nickname = request.nickname,
                bio = request.bio ?: "",
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfileResponse.from(profile))
    }

    @Operation(summary = "프로필 수정")
    @PutMapping("/me/profiles/{profileId}")
    fun updateProfile(
        @RequestHeader("X-Member-Id") memberId: Long,
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<ProfileResponse> {
        val profile = updateProfileUseCase(
            UpdateProfileCommand(
                memberId = memberId,
                profileId = profileId,
                nickname = request.nickname,
                bio = request.bio ?: "",
            )
        )
        return ResponseEntity.ok(ProfileResponse.from(profile))
    }

    @Operation(summary = "프로필 삭제")
    @DeleteMapping("/me/profiles/{profileId}")
    fun deleteProfile(
        @RequestHeader("X-Member-Id") memberId: Long,
        @PathVariable profileId: Long,
    ): ResponseEntity<Unit> {
        deleteProfileUseCase(DeleteProfileCommand(memberId = memberId, profileId = profileId))
        return ResponseEntity.noContent().build()
    }
}

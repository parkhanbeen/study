package com.anoncommunity.app.api.controller

import com.anoncommunity.app.api.security.JwtProperties
import com.anoncommunity.app.api.security.JwtTokenProvider
import com.anoncommunity.app.usecase.member.LoginCommand
import com.anoncommunity.contract.auth.RefreshTokenPort
import com.anoncommunity.app.usecase.member.LoginUseCase
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import com.anoncommunity.domain.member.port.MemberQueryPort
import com.anoncommunity.domain.member.port.ProfileQueryPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val loginUseCase: LoginUseCase,
    private val memberQueryPort: MemberQueryPort,
    private val profileQueryPort: ProfileQueryPort,
    private val refreshTokenPort: RefreshTokenPort,
) {

    @Operation(summary = "로그인 (JWT 발급)")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val member = loginUseCase(LoginCommand(
            nickname = request.nickname,
            password = request.password,
        ))

        val activeProfileId = profileQueryPort.findDefaultByMemberId(member.id)?.id?.value ?: 0L
        val accessToken = jwtTokenProvider.issueAccessToken(member.id.value, activeProfileId)
        val refreshToken = jwtTokenProvider.issueRefreshToken(member.id.value)

        refreshTokenPort.store(member.id.value, refreshToken, jwtProperties.refreshTokenExpiry)

        return ResponseEntity.ok(
            TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                memberId = member.id.value,
                activeProfileId = activeProfileId,
            )
        )
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        val memberId = jwtTokenProvider.validateRefreshToken(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid credentials")

        // Verify refresh token exists in store (reuse detection)
        if (!refreshTokenPort.validate(memberId, request.refreshToken)) {
            // Potential token reuse attack — revoke all tokens for this member
            refreshTokenPort.revoke(memberId)
            throw IllegalArgumentException("Invalid credentials")
        }

        val member = memberQueryPort.findById(MemberId(memberId))
            ?: throw MemberNotFoundException(MemberId(memberId))

        val activeProfileId = profileQueryPort.findDefaultByMemberId(member.id)?.id?.value ?: 0L
        val accessToken = jwtTokenProvider.issueAccessToken(member.id.value, activeProfileId)
        val refreshToken = jwtTokenProvider.issueRefreshToken(member.id.value)

        // Rotate: replace old refresh token with new one
        refreshTokenPort.store(member.id.value, refreshToken, jwtProperties.refreshTokenExpiry)

        return ResponseEntity.ok(
            TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                memberId = member.id.value,
                activeProfileId = activeProfileId,
            )
        )
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Unit> {
        val memberId = jwtTokenProvider.validateRefreshToken(request.refreshToken)
        memberId?.let { refreshTokenPort.revoke(it) }
        return ResponseEntity.noContent().build()
    }
}

data class LoginRequest(
    @field:NotBlank val nickname: String,
    @field:NotBlank val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long,
    val activeProfileId: Long,
)

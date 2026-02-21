package com.anoncommunity.app.api.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun issueAccessToken(memberId: Long, activeProfileId: Long): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(memberId.toString())
            .issuer(jwtProperties.issuer)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(jwtProperties.accessTokenExpiry)))
            .claim(CLAIM_ACTIVE_PROFILE_ID, activeProfileId)
            .signWith(secretKey)
            .compact()
    }

    fun issueRefreshToken(memberId: Long): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(memberId.toString())
            .issuer(jwtProperties.issuer)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(jwtProperties.refreshTokenExpiry)))
            .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
            .signWith(secretKey)
            .compact()
    }

    fun validateRefreshToken(token: String): Long? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer)
                .build()
                .parseSignedClaims(token)
                .payload

            if (claims[CLAIM_TOKEN_TYPE] != TOKEN_TYPE_REFRESH) return null
            claims.subject?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val CLAIM_TOKEN_TYPE = "type"
        private const val TOKEN_TYPE_REFRESH = "refresh"
        const val CLAIM_ACTIVE_PROFILE_ID = "activeProfileId"
    }
}

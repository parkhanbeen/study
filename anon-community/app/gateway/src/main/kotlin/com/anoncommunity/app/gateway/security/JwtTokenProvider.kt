package com.anoncommunity.app.gateway.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun validateAndExtractClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            log.debug("JWT token expired: {}", e.message)
            null
        } catch (e: JwtException) {
            log.warn("Invalid JWT token: {}", e.message)
            null
        }
    }

    fun extractMemberId(claims: Claims): Long? {
        return claims.subject?.toLongOrNull()
    }

    fun extractActiveProfileId(claims: Claims): Long? {
        return claims.get(CLAIM_ACTIVE_PROFILE_ID, Integer::class.java)?.toLong()
            ?: claims.get(CLAIM_ACTIVE_PROFILE_ID, Long::class.java)
    }

    companion object {
        private const val CLAIM_ACTIVE_PROFILE_ID = "activeProfileId"
    }
}

package com.anoncommunity.app.gateway.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

class JwtTokenProviderSpec : DescribeSpec({

    val secret = "anon-community-jwt-secret-key-for-testing-minimum-256-bits-required"
    val issuer = "anon-community"
    val properties = JwtProperties(secret = secret, issuer = issuer, accessTokenExpiry = 1800)
    val provider = JwtTokenProvider(properties)
    val secretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    describe("validateAndExtractClaims") {
        it("should validate a correctly signed token") {
            val token = Jwts.builder()
                .subject("123")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact()

            val claims = provider.validateAndExtractClaims(token)

            claims.shouldNotBeNull()
            claims.subject shouldBe "123"
        }

        it("should return null for expired token") {
            val token = Jwts.builder()
                .subject("123")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(secretKey)
                .compact()

            provider.validateAndExtractClaims(token).shouldBeNull()
        }

        it("should return null for wrong issuer") {
            val token = Jwts.builder()
                .subject("123")
                .issuer("wrong-issuer")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact()

            provider.validateAndExtractClaims(token).shouldBeNull()
        }

        it("should return null for tampered token") {
            val otherKey = Keys.hmacShaKeyFor(
                "another-secret-key-for-testing-at-least-256-bits-long-enough".toByteArray(StandardCharsets.UTF_8)
            )
            val token = Jwts.builder()
                .subject("123")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(otherKey)
                .compact()

            provider.validateAndExtractClaims(token).shouldBeNull()
        }
    }

    describe("extractMemberId") {
        it("should extract member ID from claims") {
            val token = Jwts.builder()
                .subject("456")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact()

            val claims = provider.validateAndExtractClaims(token)!!
            provider.extractMemberId(claims) shouldBe 456L
        }
    }

    describe("extractActiveProfileId") {
        it("should extract activeProfileId when present in claims") {
            val token = Jwts.builder()
                .subject("123")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .claim("activeProfileId", 77L)
                .signWith(secretKey)
                .compact()

            val claims = provider.validateAndExtractClaims(token)!!
            provider.extractActiveProfileId(claims) shouldBe 77L
        }

        it("should return null when activeProfileId is absent from claims") {
            val token = Jwts.builder()
                .subject("123")
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact()

            val claims = provider.validateAndExtractClaims(token)!!
            provider.extractActiveProfileId(claims).shouldBeNull()
        }
    }
})

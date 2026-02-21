package com.anoncommunity.app.gateway.filter

import com.anoncommunity.app.gateway.security.JwtTokenProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.Claims
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import io.mockk.*
import reactor.core.publisher.Mono

class JwtAuthenticationFilterSpec : DescribeSpec({
    val jwtTokenProvider = mockk<JwtTokenProvider>()
    val objectMapper = jacksonObjectMapper()
    val internalToken = "test-internal-token"
    val filter = JwtAuthenticationFilter(jwtTokenProvider, objectMapper, internalToken)
    val chain = mockk<GatewayFilterChain>()

    beforeEach { clearMocks(chain, jwtTokenProvider) }

    describe("JwtAuthenticationFilter") {
        context("public endpoints") {
            it("should bypass /api/v1/auth/** without token") {
                val request = MockServerHttpRequest.post("/api/v1/auth/login").build()
                val exchange = MockServerWebExchange.from(request)
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                verify(exactly = 1) { chain.filter(any()) }
                verify(exactly = 0) { jwtTokenProvider.validateAndExtractClaims(any()) }
                exchangeSlot.captured.request.headers.getFirst("X-Internal-Token") shouldBe internalToken
            }

            it("should bypass GET /api/v1/feed without token") {
                val request = MockServerHttpRequest.get("/api/v1/feed").build()
                val exchange = MockServerWebExchange.from(request)
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                verify(exactly = 1) { chain.filter(any()) }
                verify(exactly = 0) { jwtTokenProvider.validateAndExtractClaims(any()) }
                exchangeSlot.captured.request.headers.getFirst("X-Internal-Token") shouldBe internalToken
            }

            it("should bypass POST /api/v1/members without token") {
                val request = MockServerHttpRequest.post("/api/v1/members").build()
                val exchange = MockServerWebExchange.from(request)
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                verify(exactly = 1) { chain.filter(any()) }
                verify(exactly = 0) { jwtTokenProvider.validateAndExtractClaims(any()) }
            }

            it("should bypass GET /api/v1/posts/{postId} without token") {
                val request = MockServerHttpRequest.get("/api/v1/posts/123").build()
                val exchange = MockServerWebExchange.from(request)
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                verify(exactly = 1) { chain.filter(any()) }
                verify(exactly = 0) { jwtTokenProvider.validateAndExtractClaims(any()) }
            }

            it("should bypass /actuator/** without token") {
                val request = MockServerHttpRequest.get("/actuator/health").build()
                val exchange = MockServerWebExchange.from(request)
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                verify(exactly = 1) { chain.filter(any()) }
                verify(exactly = 0) { jwtTokenProvider.validateAndExtractClaims(any()) }
            }
        }

        context("protected endpoints") {
            it("should return 401 when Authorization header is missing") {
                val request = MockServerHttpRequest.post("/api/v1/posts").build()
                val exchange = MockServerWebExchange.from(request)

                filter.filter(exchange, chain).block()

                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 0) { chain.filter(any()) }
            }

            it("should return 401 when Authorization header does not start with Bearer") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Basic dXNlcjpwYXNz")
                    .build()
                val exchange = MockServerWebExchange.from(request)

                filter.filter(exchange, chain).block()

                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 0) { chain.filter(any()) }
            }

            it("should return 401 for invalid JWT") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Bearer invalid-token")
                    .build()
                val exchange = MockServerWebExchange.from(request)
                every { jwtTokenProvider.validateAndExtractClaims("invalid-token") } returns null

                filter.filter(exchange, chain).block()

                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 0) { chain.filter(any()) }
            }

            it("should add X-Member-Id and X-Internal-Token on valid JWT") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Bearer valid-token")
                    .build()
                val exchange = MockServerWebExchange.from(request)
                val claims = mockk<Claims>()
                every { jwtTokenProvider.validateAndExtractClaims("valid-token") } returns claims
                every { jwtTokenProvider.extractMemberId(claims) } returns 42L
                every { jwtTokenProvider.extractActiveProfileId(claims) } returns null
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                val headers = exchangeSlot.captured.request.headers
                headers.getFirst("X-Member-Id") shouldBe "42"
                headers.getFirst("X-Internal-Token") shouldBe internalToken
                verify(exactly = 1) { chain.filter(any()) }
            }

            it("should add X-Profile-Id header when activeProfileId is present in JWT") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Bearer valid-token")
                    .build()
                val exchange = MockServerWebExchange.from(request)
                val claims = mockk<Claims>()
                every { jwtTokenProvider.validateAndExtractClaims("valid-token") } returns claims
                every { jwtTokenProvider.extractMemberId(claims) } returns 42L
                every { jwtTokenProvider.extractActiveProfileId(claims) } returns 99L
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                val headers = exchangeSlot.captured.request.headers
                headers.getFirst("X-Member-Id") shouldBe "42"
                headers.getFirst("X-Profile-Id") shouldBe "99"
                headers.getFirst("X-Internal-Token") shouldBe internalToken
                verify(exactly = 1) { chain.filter(any()) }
            }

            it("should not add X-Profile-Id header when activeProfileId is absent from JWT") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Bearer valid-token")
                    .build()
                val exchange = MockServerWebExchange.from(request)
                val claims = mockk<Claims>()
                every { jwtTokenProvider.validateAndExtractClaims("valid-token") } returns claims
                every { jwtTokenProvider.extractMemberId(claims) } returns 42L
                every { jwtTokenProvider.extractActiveProfileId(claims) } returns null
                val exchangeSlot = slot<ServerWebExchange>()
                every { chain.filter(capture(exchangeSlot)) } returns Mono.empty()

                filter.filter(exchange, chain).block()

                val headers = exchangeSlot.captured.request.headers
                headers.getFirst("X-Profile-Id").shouldBeNull()
                verify(exactly = 1) { chain.filter(any()) }
            }

            it("should return 401 when memberId extraction returns null") {
                val request = MockServerHttpRequest.post("/api/v1/posts")
                    .header("Authorization", "Bearer valid-token")
                    .build()
                val exchange = MockServerWebExchange.from(request)
                val claims = mockk<Claims>()
                every { jwtTokenProvider.validateAndExtractClaims("valid-token") } returns claims
                every { jwtTokenProvider.extractMemberId(claims) } returns null

                filter.filter(exchange, chain).block()

                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 0) { chain.filter(any()) }
            }

            it("should not bypass non-GET /api/v1/feed") {
                val request = MockServerHttpRequest.post("/api/v1/feed").build()
                val exchange = MockServerWebExchange.from(request)

                filter.filter(exchange, chain).block()

                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 0) { chain.filter(any()) }
            }
        }

        it("should have order -2") {
            filter.order shouldBe -2
        }
    }
})

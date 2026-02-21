package com.anoncommunity.app.gateway.filter

import com.anoncommunity.app.gateway.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
    @Value("\${gateway.internal-token}") private val internalToken: String,
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)
    private val pathMatcher = AntPathMatcher()

    override fun getOrder(): Int = FILTER_ORDER

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.uri.path
        val method = request.method

        // Public endpoints bypass JWT authentication
        if (isPublicEndpoint(path, method)) {
            val mutatedRequest = request.mutate()
                .header(INTERNAL_TOKEN_HEADER, internalToken)
                .build()
            return chain.filter(exchange.mutate().request(mutatedRequest).build())
        }

        // Extract and validate JWT
        val authHeader = request.headers.getFirst(AUTHORIZATION_HEADER)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.substring(BEARER_PREFIX.length)
        val claims = jwtTokenProvider.validateAndExtractClaims(token)
            ?: return unauthorized(exchange, "Invalid or expired JWT token")

        val memberId = jwtTokenProvider.extractMemberId(claims)
            ?: return unauthorized(exchange, "Invalid member ID in token")

        val activeProfileId = jwtTokenProvider.extractActiveProfileId(claims)

        // Set X-Member-Id, X-Profile-Id and X-Internal-Token for downstream
        val requestBuilder = request.mutate()
            .header(MEMBER_ID_HEADER, memberId.toString())
            .header(INTERNAL_TOKEN_HEADER, internalToken)
        if (activeProfileId != null) {
            requestBuilder.header(PROFILE_ID_HEADER, activeProfileId.toString())
        }
        val mutatedRequest = requestBuilder.build()

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
    }

    private fun isPublicEndpoint(path: String, method: HttpMethod?): Boolean =
        PUBLIC_ENDPOINTS.any { (pattern, methods) ->
            pathMatcher.match(pattern, path) && (methods.isEmpty() || method in methods)
        }

    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        log.debug("Authentication failed: {}", message)
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON
        val body = objectMapper.writeValueAsString(
            mapOf("code" to "UNAUTHORIZED", "message" to message)
        )
        val buffer = response.bufferFactory().wrap(body.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    companion object {
        const val FILTER_ORDER = -2
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val MEMBER_ID_HEADER = "X-Member-Id"
        private const val PROFILE_ID_HEADER = "X-Profile-Id"
        private const val INTERNAL_TOKEN_HEADER = "X-Internal-Token"

        // Public endpoints: path pattern to allowed HTTP methods (empty = all methods)
        private val PUBLIC_ENDPOINTS: List<Pair<String, Set<HttpMethod>>> = listOf(
            "/api/v1/auth/**" to emptySet(),
            "/api/v1/members" to setOf(HttpMethod.POST),
            "/api/v1/feed" to setOf(HttpMethod.GET),
            "/api/v1/posts/{postId}" to setOf(HttpMethod.GET),
            "/api/v1/posts/{postId}/comments" to setOf(HttpMethod.GET),
            "/api/v1/search/**" to setOf(HttpMethod.GET),
            "/actuator/**" to emptySet(),
            "/swagger-ui/**" to emptySet(),
            "/v3/api-docs/**" to emptySet(),
        )
    }
}

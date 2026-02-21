package com.anoncommunity.app.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.MessageDigest

/**
 * Validates that requests originate from the API Gateway by checking X-Internal-Token.
 * Enabled by default; set gateway.auth.enabled=false for local development without gateway.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class GatewayAuthFilter(
    @Value("\${gateway.internal-token:}") private val expectedToken: String,
    @Value("\${gateway.auth.enabled:true}") private val enabled: Boolean,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (!enabled) {
            chain.doFilter(request, response)
            return
        }

        // Health check and actuator bypass
        if (request.requestURI.startsWith("/actuator")) {
            chain.doFilter(request, response)
            return
        }

        val token = request.getHeader(INTERNAL_TOKEN_HEADER)
        if (!constantTimeEquals(token, expectedToken)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(UNAUTHORIZED_RESPONSE)
            return
        }

        chain.doFilter(request, response)
    }

    private fun constantTimeEquals(a: String?, b: String): Boolean {
        if (a == null) return false
        return MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
    }

    companion object {
        private const val INTERNAL_TOKEN_HEADER = "X-Internal-Token"
        private const val UNAUTHORIZED_RESPONSE =
            """{"code":"UNAUTHORIZED","message":"Direct access not allowed. Use the API Gateway."}"""
    }
}

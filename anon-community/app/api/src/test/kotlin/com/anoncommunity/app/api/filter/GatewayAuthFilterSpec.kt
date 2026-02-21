package com.anoncommunity.app.api.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.PrintWriter
import java.io.StringWriter

class GatewayAuthFilterSpec : DescribeSpec({

    fun mockRequest(uri: String = "/api/v1/posts", token: String? = null) = mockk<HttpServletRequest> {
        every { requestURI } returns uri
        every { getHeader("X-Internal-Token") } returns token
        every { getAttribute(any()) } returns null
        every { setAttribute(any(), any()) } just Runs
        every { removeAttribute(any()) } just Runs
        every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
    }

    fun mockResponseWithWriter(): Pair<HttpServletResponse, StringWriter> {
        val stringWriter = StringWriter()
        val response = mockk<HttpServletResponse>(relaxed = true) {
            every { writer } returns PrintWriter(stringWriter)
        }
        return response to stringWriter
    }

    describe("GatewayAuthFilter") {

        context("when disabled") {
            it("should pass through without checking token") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = false)
                val request = mockRequest()
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(request, response) }
            }
        }

        context("when enabled") {
            val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)

            it("should bypass /actuator endpoints") {
                val request = mockRequest(uri = "/actuator/health")
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(any(), any()) }
                verify(exactly = 0) { response.status = any() }
            }

            it("should return 401 when X-Internal-Token is missing") {
                val request = mockRequest(token = null)
                val (response, body) = mockResponseWithWriter()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
                verify(exactly = 0) { chain.doFilter(any(), any()) }
                body.toString() shouldContain "UNAUTHORIZED"
            }

            it("should return 401 when token does not match") {
                val request = mockRequest(token = "wrong-token")
                val (response, _) = mockResponseWithWriter()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
                verify(exactly = 0) { chain.doFilter(any(), any()) }
            }

            it("should pass through when token matches") {
                val request = mockRequest(token = "secret")
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(any(), any()) }
                verify(exactly = 0) { response.status = any() }
            }

            it("should write JSON error response body") {
                val request = mockRequest(token = null)
                val (response, body) = mockResponseWithWriter()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                body.toString() shouldContain "UNAUTHORIZED"
                body.toString() shouldContain "Direct access not allowed"
                verify { response.contentType = "application/json" }
            }
        }
    }
})

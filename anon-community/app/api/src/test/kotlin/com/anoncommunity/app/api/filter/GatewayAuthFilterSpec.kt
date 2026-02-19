package com.anoncommunity.app.api.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.PrintWriter
import java.io.StringWriter

class GatewayAuthFilterSpec : DescribeSpec({

    describe("GatewayAuthFilter") {

        context("when disabled") {
            it("should pass through without checking token") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = false)
                val request = mockk<HttpServletRequest> {
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(request, response) }
            }
        }

        context("when enabled") {

            it("should bypass /actuator endpoints") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)
                val request = mockk<HttpServletRequest> {
                    every { requestURI } returns "/actuator/health"
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(any(), any()) }
                verify(exactly = 0) { response.status = any() }
            }

            it("should return 401 when X-Internal-Token is missing") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                val request = mockk<HttpServletRequest> {
                    every { requestURI } returns "/api/v1/posts"
                    every { getHeader("X-Internal-Token") } returns null
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>(relaxed = true) {
                    every { writer } returns printWriter
                }
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
                verify(exactly = 0) { chain.doFilter(any(), any()) }
                stringWriter.toString() shouldContain "UNAUTHORIZED"
            }

            it("should return 401 when token does not match") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                val request = mockk<HttpServletRequest> {
                    every { requestURI } returns "/api/v1/posts"
                    every { getHeader("X-Internal-Token") } returns "wrong-token"
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>(relaxed = true) {
                    every { writer } returns printWriter
                }
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
                verify(exactly = 0) { chain.doFilter(any(), any()) }
            }

            it("should pass through when token matches") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)
                val request = mockk<HttpServletRequest> {
                    every { requestURI } returns "/api/v1/posts"
                    every { getHeader("X-Internal-Token") } returns "secret"
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>()
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                verify { chain.doFilter(any(), any()) }
                verify(exactly = 0) { response.status = any() }
            }

            it("should write JSON error response body") {
                val filter = GatewayAuthFilter(expectedToken = "secret", enabled = true)
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                val request = mockk<HttpServletRequest> {
                    every { requestURI } returns "/api/v1/posts"
                    every { getHeader("X-Internal-Token") } returns null
                    every { getAttribute(any()) } returns null
                    every { setAttribute(any(), any()) } just Runs
                    every { removeAttribute(any()) } just Runs
                    every { dispatcherType } returns jakarta.servlet.DispatcherType.REQUEST
                }
                val response = mockk<HttpServletResponse>(relaxed = true) {
                    every { writer } returns printWriter
                }
                val chain = mockk<FilterChain>(relaxed = true)

                filter.doFilter(request, response, chain)

                val body = stringWriter.toString()
                body shouldContain "UNAUTHORIZED"
                body shouldContain "Direct access not allowed"

                verify { response.contentType = "application/json" }
            }
        }
    }
})

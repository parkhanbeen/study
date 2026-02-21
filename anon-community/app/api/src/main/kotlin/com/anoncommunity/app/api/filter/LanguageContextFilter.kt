package com.anoncommunity.app.api.filter

import com.anoncommunity.app.api.config.LanguageContext
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Reads X-Language and X-Country headers (set by API Gateway) and stores them
 * in LanguageContext thread-local for the duration of the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class LanguageContextFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        try {
            request.getHeader(X_LANGUAGE_HEADER)?.let { LanguageContext.setLanguage(it) }
            request.getHeader(X_COUNTRY_HEADER)?.let { LanguageContext.setCountry(it) }
            chain.doFilter(request, response)
        } finally {
            LanguageContext.clear()
        }
    }

    companion object {
        private const val X_LANGUAGE_HEADER = "X-Language"
        private const val X_COUNTRY_HEADER = "X-Country"
    }
}

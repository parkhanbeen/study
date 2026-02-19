package com.anoncommunity.app.gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

@Configuration
class RouteConfig(
    @Value("\${gateway.downstream.api-uri}") private val apiUri: String,
    private val authenticatedRateLimiter: RedisRateLimiter,
    private val authRateLimiter: RedisRateLimiter,
    private val publicReadRateLimiter: RedisRateLimiter,
    private val memberIdKeyResolver: KeyResolver,
    private val ipKeyResolver: KeyResolver,
) {

    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator = builder.routes {

        // Auth endpoints (public, strict rate limit to prevent brute-force)
        route("auth") {
            path("/api/v1/auth/**")
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
            }
            uri(apiUri)
        }

        // Member creation (public, strict rate limit)
        route("member-create") {
            path("/api/v1/members") and method(HttpMethod.POST)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
            }
            uri(apiUri)
        }

        // Member update (protected, rate limited)
        route("member-update") {
            path("/api/v1/members/me/**") and method(HttpMethod.PUT)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authenticatedRateLimiter)
                    it.setKeyResolver(memberIdKeyResolver)
                }
            }
            uri(apiUri)
        }

        // Feed (public, read-only, generous rate limit)
        route("feed") {
            path("/api/v1/feed")
            filters {
                requestRateLimiter {
                    it.setRateLimiter(publicReadRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_READ)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Post read (public, generous rate limit)
        route("post-read") {
            path("/api/v1/posts/{postId}") and method(HttpMethod.GET)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(publicReadRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_READ)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Comment read (public, generous rate limit)
        route("comment-read") {
            path("/api/v1/posts/{postId}/comments") and method(HttpMethod.GET)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(publicReadRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_READ)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Search (public, generous rate limit)
        route("search") {
            path("/api/v1/search/**")
            filters {
                requestRateLimiter {
                    it.setRateLimiter(publicReadRateLimiter)
                    it.setKeyResolver(ipKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_READ)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Post write operations (protected, rate limited)
        route("post-write") {
            path("/api/v1/posts/**") and method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authenticatedRateLimiter)
                    it.setKeyResolver(memberIdKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_WRITE)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Comment write operations (protected, rate limited)
        route("comment-write") {
            path("/api/v1/posts/{postId}/comments/**") and method(HttpMethod.POST, HttpMethod.DELETE)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authenticatedRateLimiter)
                    it.setKeyResolver(memberIdKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_WRITE)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Like operations (protected, rate limited)
        route("like-write") {
            path("/api/v1/posts/{postId}/likes") and method(HttpMethod.POST, HttpMethod.DELETE)
            filters {
                requestRateLimiter {
                    it.setRateLimiter(authenticatedRateLimiter)
                    it.setKeyResolver(memberIdKeyResolver)
                }
                circuitBreaker {
                    it.setName(CB_WRITE)
                    it.setFallbackUri(FALLBACK_URI)
                }
            }
            uri(apiUri)
        }

        // Swagger / OpenAPI passthrough
        route("swagger") {
            path("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**")
            uri(apiUri)
        }
    }

    companion object {
        private const val CB_READ = "anon-api-read"
        private const val CB_WRITE = "anon-api-write"
        private const val FALLBACK_URI = "forward:/fallback"
    }
}

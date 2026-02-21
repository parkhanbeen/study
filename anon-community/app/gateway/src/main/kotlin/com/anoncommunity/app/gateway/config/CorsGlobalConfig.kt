package com.anoncommunity.app.gateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class CorsGlobalConfig(
    @Value("\${gateway.cors.allowed-origins:http://localhost:3000}") private val allowedOrigins: String,
) {

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val origins = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val config = CorsConfiguration().apply {
            // Use allowedOrigins (not Patterns) when allowCredentials = true
            // to avoid wildcard + credentials CORS vulnerability
            setAllowedOrigins(origins)
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Request-ID", "Accept-Language")
            exposedHeaders = listOf("X-Request-ID")
            allowCredentials = true
            maxAge = 3600L
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", config)
        }

        return CorsWebFilter(source)
    }
}

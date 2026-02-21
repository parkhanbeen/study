package com.anoncommunity.app.api.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val issuer: String = "anon-community",
    val accessTokenExpiry: Long = 1800,
    val refreshTokenExpiry: Long = 604800,
)

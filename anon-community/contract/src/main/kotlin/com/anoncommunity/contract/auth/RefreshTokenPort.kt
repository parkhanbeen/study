package com.anoncommunity.contract.auth

interface RefreshTokenPort {
    fun store(memberId: Long, refreshToken: String, ttlSeconds: Long)
    fun validate(memberId: Long, refreshToken: String): Boolean
    fun revoke(memberId: Long)
}

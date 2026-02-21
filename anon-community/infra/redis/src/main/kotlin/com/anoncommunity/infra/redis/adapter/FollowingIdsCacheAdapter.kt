package com.anoncommunity.infra.redis.adapter

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.FollowingIdsCache
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class FollowingIdsCacheAdapter(private val redisTemplate: StringRedisTemplate) : FollowingIdsCache {
    private val ttl = Duration.ofMinutes(10)

    override fun get(profileId: ProfileId): List<Long>? {
        return try {
            val value = redisTemplate.opsForValue().get(key(profileId)) ?: return null
            // ⚠️ Empty string guard: ids=[] → joinToString → "" → split → [""] → toLong() → NumberFormatException
            if (value.isBlank()) emptyList() else value.split(",").map { it.toLong() }
        } catch (e: Exception) {
            log.warn("Redis get failed for key {}: {}", key(profileId), e.message)
            null  // fallback to DB
        }
    }

    override fun set(profileId: ProfileId, ids: List<Long>) {
        try {
            redisTemplate.opsForValue().set(key(profileId), ids.joinToString(","), ttl)
        } catch (e: Exception) {
            log.warn("Redis set failed for key {}: {}", key(profileId), e.message)
        }
    }

    override fun invalidate(profileId: ProfileId) {
        try {
            redisTemplate.delete(key(profileId))
        } catch (e: Exception) {
            log.warn("Redis delete failed for key {}: {}", key(profileId), e.message)
        }
    }

    private fun key(profileId: ProfileId) = "$KEY_PREFIX${profileId.value}"

    companion object {
        private val log = LoggerFactory.getLogger(FollowingIdsCacheAdapter::class.java)
        private const val KEY_PREFIX = "following:"
    }
}

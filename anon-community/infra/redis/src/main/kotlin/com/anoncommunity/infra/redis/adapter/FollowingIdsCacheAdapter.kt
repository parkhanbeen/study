package com.anoncommunity.infra.redis.adapter

import com.anoncommunity.contract.model.ProfileId
import com.anoncommunity.domain.member.port.FollowingIdsCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class FollowingIdsCacheAdapter(private val redisTemplate: StringRedisTemplate) : FollowingIdsCache {
    private val ttl = Duration.ofMinutes(10)

    private fun key(profileId: ProfileId) = "following:${profileId.value}"

    override fun get(profileId: ProfileId): List<Long>? {
        val value = redisTemplate.opsForValue().get(key(profileId)) ?: return null
        // ⚠️ Empty string guard: ids=[] → joinToString → "" → split → [""] → toLong() → NumberFormatException
        return if (value.isBlank()) emptyList() else value.split(",").map { it.toLong() }
    }

    override fun set(profileId: ProfileId, ids: List<Long>) {
        redisTemplate.opsForValue().set(key(profileId), ids.joinToString(","), ttl)
    }

    override fun invalidate(profileId: ProfileId) {
        redisTemplate.delete(key(profileId))
    }
}

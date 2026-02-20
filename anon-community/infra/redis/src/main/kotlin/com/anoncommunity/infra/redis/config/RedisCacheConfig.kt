package com.anoncommunity.infra.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig(
    @Value("\${cache.feed.ttl-seconds:60}") private val feedTtl: Long,
    @Value("\${cache.post.ttl-seconds:300}") private val postTtl: Long,
    @Value("\${cache.search.ttl-seconds:10}") private val searchTtl: Long,
    @Value("\${cache.hot-tags.ttl-seconds:3600}") private val hotTagsTtl: Long,
) {

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): RedisCacheManager {
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(serializationPair)
            .disableCachingNullValues()
            .entryTtl(Duration.ofMinutes(5))

        val cacheConfigs = mapOf(
            CACHE_FEED to defaultConfig.entryTtl(Duration.ofSeconds(feedTtl)),
            CACHE_POST_DETAIL to defaultConfig.entryTtl(Duration.ofSeconds(postTtl)),
            CACHE_SEARCH to defaultConfig.entryTtl(Duration.ofSeconds(searchTtl)),
            CACHE_HOT_TAGS to defaultConfig.entryTtl(Duration.ofSeconds(hotTagsTtl)),
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .transactionAware()
            .build()
    }

    companion object {
        const val CACHE_FEED = "feed"
        const val CACHE_POST_DETAIL = "post-detail"
        const val CACHE_SEARCH = "search"
        const val CACHE_HOT_TAGS = "hot-tags"
    }
}

package com.lendingplatform.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Defines the shared RedisTemplate<String, Object> used by LenderCacheService
 * and IdempotencyService to store arbitrary Java objects (LenderRules,
 * IdempotencyRecord) as JSON in Redis.
 *
 * Keys are always plain strings (StringRedisSerializer); values are
 * serialized to/from JSON via Jackson using the app's own ObjectMapper bean
 * (auto-configured by Spring Boot, so it already understands java.time
 * types etc.) rather than a separate, disconnected ObjectMapper instance.
 *
 * Note: rate limiting (RateLimiterService) deliberately does NOT use this
 * bean - it uses Spring Boot's separately auto-configured StringRedisTemplate
 * instead, because Redis's INCR command needs a raw numeric string, not a
 * JSON-wrapped value.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                         ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }
}

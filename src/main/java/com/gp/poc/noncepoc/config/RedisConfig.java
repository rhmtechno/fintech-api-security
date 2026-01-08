package com.gp.poc.noncepoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public io.github.bucket4j.distributed.proxy.ProxyManager<byte[]> lettuceProxyManager(
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = (io.lettuce.core.RedisClient) ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) connectionFactory)
                .getNativeClient();
        return io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(java.time.Duration.ofSeconds(60)))
                .build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
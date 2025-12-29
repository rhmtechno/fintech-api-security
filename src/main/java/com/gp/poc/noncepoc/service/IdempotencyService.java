package com.gp.poc.noncepoc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration TTL = Duration.ofHours(24);
    private static final String IN_PROGRESS = "IN_PROGRESS";

    public boolean tryAcquire(String key) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, IN_PROGRESS, TTL);
        return Boolean.TRUE.equals(success);
    }

    public void storeResult(String key, String jsonResponse) {
        redisTemplate.opsForValue().set(key, jsonResponse, TTL);
    }

    public Optional<String> getResult(String key) {
        String value = redisTemplate.opsForValue().get(key);

        if (value == null || IN_PROGRESS.equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
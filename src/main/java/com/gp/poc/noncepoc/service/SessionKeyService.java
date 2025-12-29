package com.gp.poc.noncepoc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionKeyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long SESSION_TTL_SECONDS = 600;

    public String issueSessionKey(String userId, String deviceId) {
        String sessionKey = UUID.randomUUID()
                .toString()
                .replace("-", "");

        redisTemplate.opsForValue().set(
                buildKey(userId, deviceId),
                sessionKey,
                SESSION_TTL_SECONDS,
                TimeUnit.SECONDS
        );

        return sessionKey;
    }

    public String getSessionKey(String userId, String deviceId) {
        return redisTemplate.opsForValue()
                .get(buildKey(userId, deviceId));
    }

    private String buildKey(String userId, String deviceId) {
        return "session:" + userId + ":" + deviceId;
    }
}
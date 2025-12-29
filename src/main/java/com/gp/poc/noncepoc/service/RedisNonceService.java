package com.gp.poc.noncepoc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisNonceService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isReplay(String nonce) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(nonce));
    }

    public void storeNonce(String nonce, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(nonce, "USED", ttlSeconds, TimeUnit.SECONDS);
    }
}
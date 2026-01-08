package com.gp.poc.noncepoc.service;

import com.gp.poc.noncepoc.config.RateLimitingProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitingService {

    private final ProxyManager<byte[]> proxyManager;
    private final RateLimitingProperties properties;

    public RateLimitingService(ProxyManager<byte[]> proxyManager, RateLimitingProperties properties) {
        this.proxyManager = proxyManager;
        this.properties = properties;
    }

    public io.github.bucket4j.Bucket resolveBucket(String key, String policyName) {
        Supplier<BucketConfiguration> configSupplier = getConfigSupplier(policyName);
        // Composite key: policyName:ip
        String compositeKey = policyName + ":" + key;
        return proxyManager.builder().build(compositeKey.getBytes(StandardCharsets.UTF_8), configSupplier);
    }

    private Supplier<BucketConfiguration> getConfigSupplier(String policyName) {
        RateLimitingProperties.Policy policy = properties.getPolicies().getOrDefault(policyName,
                properties.getDefaultPolicy());
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(policy.getCapacity())
                        .refillIntervally(policy.getRefillTokens(),
                                Duration.ofSeconds(policy.getRefillDurationSeconds()))
                        .build())
                .build();
    }
}

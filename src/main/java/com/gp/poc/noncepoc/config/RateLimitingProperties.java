package com.gp.poc.noncepoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitingProperties {

    private boolean enabled = true;
    private Policy defaultPolicy = new Policy();
    private Map<String, Policy> policies = new HashMap<>();

    @Data
    public static class Policy {
        private int capacity = 10;
        private int refillTokens = 10;
        private int refillDurationSeconds = 60;
    }
}

package com.spendsense.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    // Caffeine-backed cache: evicts buckets after 10 minutes of inactivity
    // This prevents unbounded memory growth from unique IPs
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build();

    public Bucket resolveBucket(String key) {
        return buckets.get(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

package com.spendsense.config;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;
    private final Map<String, Bucket> buckets=new ConcurrentHashMap<>();
    public Bucket resolveBucket(String Key) {
        return buckets.computeIfAbsent(Key,k->createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit=Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

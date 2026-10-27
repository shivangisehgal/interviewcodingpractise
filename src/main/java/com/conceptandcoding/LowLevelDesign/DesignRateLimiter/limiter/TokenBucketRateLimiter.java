package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

public final class TokenBucketRateLimiter extends RateLimiter {
    private record Bucket(double tokens, long updatedAt) { }
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(RateLimitConfig config) {
        this(config, System::currentTimeMillis);
    }

    public TokenBucketRateLimiter(RateLimitConfig config, LongSupplier clockMillis) {
        super(config, RateLimitType.TOKEN_BUCKET, clockMillis);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        AtomicBoolean allowed = new AtomicBoolean();
        buckets.compute(userId, (id, previous) -> {
            long now = clockMillis.getAsLong();
            Bucket before = previous == null ? new Bucket(config.getMaxRequests(), now) : previous;
            now = Math.max(now, before.updatedAt());
            double refill = (now - before.updatedAt())
                    * (double) config.getMaxRequests() / (config.getWindowInSeconds() * 1000L);
            double available = Math.min(config.getMaxRequests(), before.tokens() + refill);
            if (available >= 1.0) {
                allowed.set(true);
                available -= 1.0;
            }
            // Preserve fractional tokens instead of discarding partial refill intervals.
            return new Bucket(available, now);
        });
        return allowed.get();
    }
}

package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;
import java.util.Objects;
import java.util.function.LongSupplier;

public abstract class RateLimiter {
    protected final RateLimitConfig config;
    protected final RateLimitType type;
    protected final LongSupplier clockMillis;

    protected RateLimiter(RateLimitConfig config, RateLimitType type, LongSupplier clockMillis) {
        this.config = Objects.requireNonNull(config, "config");
        this.type = Objects.requireNonNull(type, "type");
        this.clockMillis = Objects.requireNonNull(clockMillis, "clockMillis");
    }

    public abstract boolean allowRequest(String userId);
}

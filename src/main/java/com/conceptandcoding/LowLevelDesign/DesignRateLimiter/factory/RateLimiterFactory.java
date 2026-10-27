package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.factory;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter.*;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;

public class RateLimiterFactory {
    public static RateLimiter createRateLimiter(RateLimitType algo, RateLimitConfig config) {
        return switch (algo) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(config);
            case FIXED_WINDOW -> new FixedWindowRateLimiter(config);
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogRateLimiter(config);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
        };
    }
}

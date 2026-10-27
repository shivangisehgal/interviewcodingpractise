package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums;

public enum RateLimitType {
    TOKEN_BUCKET,
    LEAKY_BUCKET,
    FIXED_WINDOW,
    SLIDING_WINDOW_LOG,
    SLIDING_WINDOW_COUNTER
}

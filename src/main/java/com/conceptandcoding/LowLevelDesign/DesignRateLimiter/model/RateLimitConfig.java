package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model;

public final class RateLimitConfig {
    private final int maxRequests;
    private final int windowInSeconds;

    public RateLimitConfig(int maxRequests, int windowInSeconds) {
        if (maxRequests <= 0 || windowInSeconds <= 0) {
            throw new IllegalArgumentException("Request limit and window must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowInSeconds = windowInSeconds;
    }

    public int getMaxRequests() { return maxRequests; }
    public int getWindowInSeconds() { return windowInSeconds; }
}

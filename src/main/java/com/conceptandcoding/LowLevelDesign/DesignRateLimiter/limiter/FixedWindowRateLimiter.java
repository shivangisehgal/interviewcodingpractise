package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

public final class FixedWindowRateLimiter extends RateLimiter {
    private record Window(long number, int requests) { }
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(RateLimitConfig config) {
        this(config, System::currentTimeMillis);
    }

    public FixedWindowRateLimiter(RateLimitConfig config, LongSupplier clockMillis) {
        super(config, RateLimitType.FIXED_WINDOW, clockMillis);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        AtomicBoolean allowed = new AtomicBoolean();
        windows.compute(userId, (id, previous) -> {
            long number = Math.floorDiv(clockMillis.getAsLong(), config.getWindowInSeconds() * 1000L);
            // Keep the first window with its count and update both atomically.
            Window current = previous == null || number > previous.number()
                    ? new Window(number, 0) : previous;
            if (current.requests() < config.getMaxRequests()) {
                allowed.set(true);
                return new Window(current.number(), current.requests() + 1);
            }
            return current;
        });
        return allowed.get();
    }
}

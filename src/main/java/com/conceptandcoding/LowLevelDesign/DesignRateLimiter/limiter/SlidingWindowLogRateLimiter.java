package com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

public final class SlidingWindowLogRateLimiter extends RateLimiter {
    private final ConcurrentHashMap<String, ArrayDeque<Long>> requestLog = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(RateLimitConfig config) {
        this(config, System::currentTimeMillis);
    }

    public SlidingWindowLogRateLimiter(RateLimitConfig config, LongSupplier clockMillis) {
        super(config, RateLimitType.SLIDING_WINDOW_LOG, clockMillis);
    }

    @Override
    public boolean allowRequest(String userId) {
        Objects.requireNonNull(userId, "userId");
        AtomicBoolean allowed = new AtomicBoolean();
        requestLog.compute(userId, (id, log) -> {
            if (log == null) log = new ArrayDeque<>();
            long now = clockMillis.getAsLong();
            if (!log.isEmpty()) now = Math.max(now, log.peekLast());
            long duration = config.getWindowInSeconds() * 1000L;
            while (!log.isEmpty() && now - log.peekFirst() >= duration) log.removeFirst();
            if (log.size() < config.getMaxRequests()) {
                log.addLast(now);
                allowed.set(true);
            }
            return log;
        });
        return allowed.get();
    }
}

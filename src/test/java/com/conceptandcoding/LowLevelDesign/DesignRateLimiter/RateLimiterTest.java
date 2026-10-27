package com.conceptandcoding.LowLevelDesign.DesignRateLimiter;

import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.RateLimitType;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.enums.UserTier;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.factory.RateLimiterFactory;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.limiter.*;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.RateLimitConfig;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.model.User;
import com.conceptandcoding.LowLevelDesign.DesignRateLimiter.service.RateLimiterService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Standalone tests with controlled time; no sleeps or Spring context. */
public final class RateLimiterTest {
    public static void main(String[] args) throws Exception {
        expect(IllegalArgumentException.class, () -> new RateLimitConfig(0, 1));
        expect(IllegalArgumentException.class, () -> new RateLimitConfig(1, 0));
        expect(IllegalArgumentException.class, () -> new RateLimitConfig(-1, 1));
        expect(IllegalArgumentException.class, () -> new RateLimitConfig(1, -1));
        RateLimitConfig config = new RateLimitConfig(2, 1);
        AtomicLong clock = new AtomicLong();
        FixedWindowRateLimiter fixed = new FixedWindowRateLimiter(config, clock::get);
        drain(fixed, "a", 2);
        drain(fixed, "b", 2);
        clock.set(999);
        check(!fixed.allowRequest("a"), "Fixed window reset too early");
        clock.set(1000);
        drain(fixed, "a", 2);
        clock.set(2000);
        drain(fixed, "a", 2);
        clock.set(0);
        check(!fixed.allowRequest("a"), "Clock rollback must not reset quota");

        clock.set(0);
        SlidingWindowLogRateLimiter sliding = new SlidingWindowLogRateLimiter(config, clock::get);
        check(sliding.allowRequest("a"), "First request must pass");
        clock.set(500);
        check(sliding.allowRequest("a"), "Second request must pass");
        clock.set(999);
        check(!sliding.allowRequest("a"), "Sliding window reset too early");
        clock.set(1000);
        check(sliding.allowRequest("a"), "Oldest request should expire at boundary");
        check(!sliding.allowRequest("a"), "Only one slot should expire");
        clock.set(1500);
        check(sliding.allowRequest("a"), "Second slot should expire");
        check(!sliding.allowRequest("a"), "Rejected requests must not consume slots");

        clock.set(0);
        TokenBucketRateLimiter token = new TokenBucketRateLimiter(config, clock::get);
        drain(token, "a", 2);
        clock.set(250);
        check(!token.allowRequest("a"), "Half a token must not admit a request");
        clock.set(500);
        check(token.allowRequest("a"), "Fractional refill must accumulate");
        check(!token.allowRequest("a"), "Token must be consumed");
        clock.set(750);
        check(!token.allowRequest("a"), "Partial refill must be retained");
        clock.set(1000);
        check(token.allowRequest("a"), "Repeated partial refills must accumulate");
        clock.set(100000);
        drain(token, "a", 2);
        clock.set(200000);
        RateLimiter fast = new TokenBucketRateLimiter(new RateLimitConfig(100, 60), clock::get);
        drain(fast, "a", 100);
        clock.addAndGet(600);
        check(fast.allowRequest("a"), "Subsecond refill must work");

        for (RateLimitType type : List.of(RateLimitType.TOKEN_BUCKET,
                RateLimitType.FIXED_WINDOW, RateLimitType.SLIDING_WINDOW_LOG)) {
            RateLimiter made = RateLimiterFactory.createRateLimiter(type, config);
            check(made.allowRequest("factory"), "Factory must create supported algorithm");
        }
        expect(IllegalArgumentException.class, () -> RateLimiterFactory.createRateLimiter(
                RateLimitType.LEAKY_BUCKET, config));
        expect(IllegalArgumentException.class, () -> RateLimiterFactory.createRateLimiter(
                RateLimitType.SLIDING_WINDOW_COUNTER, config));
        expect(NullPointerException.class, () -> token.allowRequest(null));
        RateLimiterService service = new RateLimiterService();
        check(service.allowRequest(new User("free", UserTier.FREE)), "Free tier must be configured");
        check(service.allowRequest(new User("premium", UserTier.PREMIUM)), "Premium tier must be configured");

        RateLimitConfig concurrentConfig = new RateLimitConfig(10, 60);
        concurrent(new FixedWindowRateLimiter(concurrentConfig, () -> 0));
        concurrent(new SlidingWindowLogRateLimiter(concurrentConfig, () -> 0));
        concurrent(new TokenBucketRateLimiter(concurrentConfig, () -> 0));
        System.out.println("Rate limiter tests passed");
    }

    private static void drain(RateLimiter limiter, String user, int limit) {
        for (int i = 0; i < limit; i++) check(limiter.allowRequest(user), "Request blocked before limit");
        check(!limiter.allowRequest(user), "Request admitted over limit");
    }

    private static void concurrent(RateLimiter limiter) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 16; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    int allowed = 0;
                    for (int j = 0; j < 10; j++) if (limiter.allowRequest("shared")) allowed++;
                    return allowed;
                }));
            }
            start.countDown();
            int allowed = 0;
            for (Future<Integer> result : results) allowed += result.get(10, TimeUnit.SECONDS);
            check(allowed == 10, "Concurrent requests must share exactly one quota");
            drain(limiter, "independent", 10);
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers did not stop");
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

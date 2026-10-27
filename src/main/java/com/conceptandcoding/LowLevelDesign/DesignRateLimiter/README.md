# Design Rate Limiter

Imported and adapted from [shubhkpatel/Low-Level-Design: rate_limiter](https://github.com/shubhkpatel/Low-Level-Design/tree/fd753ebd1fc03b6a499c8671151f62ed7dc13c05/src/main/java/org/nailyourinterview/lld/rate_limiter),
upstream commit `fd753ebd1fc03b6a499c8671151f62ed7dc13c05`. Original implementation credit: shubhkpatel.

## Included design

- `RateLimiter` defines the common API; `RateLimiterFactory` selects a strategy.
- `TokenBucketRateLimiter` starts each user with a full bucket and replenishes
  tokens continuously at `maxRequests / windowInSeconds`, capped at capacity.
- `FixedWindowRateLimiter` counts requests in epoch-aligned windows.
- `SlidingWindowLogRateLimiter` tracks accepted timestamps in a rolling window.
- `RateLimiterService` configures FREE users for a 10-token bucket replenished over
  60 seconds and PREMIUM users for 100 requests per fixed 60-second window.
  Each user has an independent quota within its tier.
- Upstream enum placeholders `LEAKY_BUCKET` and `SLIDING_WINDOW_COUNTER` remain
  unsupported and the factory rejects them.

## Integration changes

Packages now live under `com.conceptandcoding.LowLevelDesign.DesignRateLimiter`.
Lombok constructors/getters are replaced with ordinary Java, avoiding a new dependency.
Configuration requires positive limits/windows and user fields cannot be null.
Fixed-window state records the initial window and count together, fixing the
upstream reset bug and the shared unsynchronized window map. Token refill keeps
fractional tokens, including subsecond intervals. Each algorithm samples time
inside its per-user atomic update and exposes a millisecond `LongSupplier`
constructor for deterministic tests. Backward clock samples cannot create quota.
The demo counts completed tasks in a `finally` block.

These are in-memory, single-process examples. State is retained for every user
seen; there is no idle-user cleanup or distributed coordination. Fixed windows
permit boundary bursts; token buckets permit an initial burst. Default time comes
from the wall clock, so forward clock jumps can advance quotas. Supply a controlled
clock in tests. Sliding-log storage is bounded by the configured quota per user.

## Compile and run from the repository root (Java 17)

```sh
mkdir -p target/rate-limiter
find src/main/java/com/conceptandcoding/LowLevelDesign/DesignRateLimiter -name '*.java' > target/rate-limiter/sources.txt
javac -d target/rate-limiter @target/rate-limiter/sources.txt
java -cp target/rate-limiter com.conceptandcoding.LowLevelDesign.DesignRateLimiter.Main
```

The demo sends 20 concurrent requests for one FREE user; a fresh bucket admits 10
and blocks 10 when the requests complete before the next token refill.

## Focused tests

```sh
javac -cp target/rate-limiter -d target/rate-limiter src/test/java/com/conceptandcoding/LowLevelDesign/DesignRateLimiter/RateLimiterTest.java
java -cp target/rate-limiter com.conceptandcoding.LowLevelDesign.DesignRateLimiter.RateLimiterTest
```

Tests cover initial quotas, independent users, fixed-window rollover, rolling
expiry boundaries, fractional/subsecond token refill, bucket capacity, validation,
factory routing, tier configuration and concurrent enforcement for all three algorithms.

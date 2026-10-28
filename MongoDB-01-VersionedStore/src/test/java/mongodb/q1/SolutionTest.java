package mongodb.q1;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SolutionTest {
    public static void main(String[] args) throws Exception {
        VersionedStore store = new VersionedStore();
        check(store.get("missing", 0) == null, "Unknown key must miss");
        store.set("a", "Alice", 5);
        store.delete("a", 10);
        store.set("a", "Bob", 15);
        check(store.get("a", 3) == null, "Before first version");
        check(store.get("a", 5).equals("Alice") && store.get("a", 9).equals("Alice"), "Floor lookup");
        check(store.get("a", 10) == null && store.get("a", 14) == null, "Tombstone lookup");
        check(store.get("a", 15).equals("Bob") && store.get("a", Long.MAX_VALUE).equals("Bob"), "Resurrection");
        store.delete("never", Long.MIN_VALUE);
        check(store.get("never", 0) == null, "Delete unknown key");
        expect(IllegalArgumentException.class, () -> store.set("a", "bad", 15));
        expect(IllegalArgumentException.class, () -> store.delete("a", 14));
        expect(NullPointerException.class, () -> store.set("a", null, 16));
        check(store.get("a", 100).equals("Bob"), "Rejected write changed history");
        Random random = new Random(1);
        TreeMap<Long, String> reference = new TreeMap<>();
        long timestamp = -1000;
        for (int i = 0; i < 2000; i++) {
            timestamp += random.nextInt(4) + 1;
            String value = random.nextInt(4) == 0 ? null : "v" + i;
            if (value == null) store.delete("random", timestamp);
            else store.set("random", value, timestamp);
            reference.put(timestamp, value);
            long query = random.nextInt(10000) - 1000;
            var entry = reference.floorEntry(query);
            check(Objects.equals(store.get("random", query), entry == null ? null : entry.getValue()), "Reference mismatch");
        }
        concurrent();
        System.out.println("MongoDB-01 tests passed");
    }
    private static void concurrent() throws Exception {
        VersionedStore store = new VersionedStore();
        store.set("shared", "base", 0);
        ExecutorService pool = Executors.newFixedThreadPool(6);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            futures.add(pool.submit(() -> {
                start.await();
                for (int i = 1; i <= 2000; i++) store.set("shared", "v" + i, i);
                return null;
            }));
            for (int worker = 0; worker < 5; worker++) {
                final String key = "writer" + worker;
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 1; i <= 500; i++) {
                        store.set(key, "v" + i, i);
                        check(store.get(key, i).equals("v" + i), "Concurrent writer lost value");
                        check(store.get("shared", 0).equals("base"), "Historical read changed");
                        check(store.get("shared", Long.MAX_VALUE) != null, "Torn latest read");
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) future.get(10, TimeUnit.SECONDS);
            check(store.get("shared", 2000).equals("v2000"), "Missing final write");
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers did not stop");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}

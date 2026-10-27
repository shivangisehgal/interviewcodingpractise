package com.conceptandcoding.LowLevelDesign.DesignLRUCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Standalone tests requiring only Java 17. */
public final class LRUCacheTest {
    public static void main(String[] args) throws Exception {
        expect(IllegalArgumentException.class, () -> new LRUCache<>(0));
        expect(IllegalArgumentException.class, () -> new LRUCache<>(-1));
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        check(cache.get("missing") == null, "Empty cache must miss");
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");
        cache.put("c", 3);
        check(cache.get("b") == null, "A hit must protect the accessed entry from eviction");
        cache.put("a", 10);
        check(cache.size() == 2, "Updates must not increase size");
        cache.put("d", 4);
        check(cache.get("c") == null && cache.get("a") == 10, "Updates must refresh recency");
        List<String> before = cache.keysMostRecentFirst();
        cache.get("missing");
        check(before.equals(cache.keysMostRecentFirst()), "Misses must preserve recency");
        expect(UnsupportedOperationException.class, () -> before.add("mutate"));
        expect(NullPointerException.class, () -> cache.put(null, 1));
        expect(NullPointerException.class, () -> cache.put("a", null));
        expect(NullPointerException.class, () -> cache.get(null));
        check(before.equals(cache.keysMostRecentFirst()), "Rejected operations must preserve recency");
        LRUCache<Integer, Integer> single = new LRUCache<>(1);
        single.put(1, 1);
        single.put(1, 2);
        single.put(2, 3);
        check(single.get(1) == null && single.get(2) == 3 && single.size() == 1,
                "Capacity-one replacement must work");
        compareWithReference();
        concurrentAccess();
        System.out.println("LRU cache tests passed");
    }

    private static void compareWithReference() {
        for (int capacity : new int[] {1, 2, 7, 32}) {
            LRUCache<Integer, Integer> cache = new LRUCache<>(capacity);
            LinkedHashMap<Integer, Integer> reference = new LinkedHashMap<>(16, 0.75f, true);
            Random random = new Random(42 + capacity);
            for (int i = 0; i < 10000; i++) {
                int key = random.nextInt(50);
                if (random.nextBoolean()) {
                    cache.put(key, i);
                    reference.put(key, i);
                    if (reference.size() > capacity) {
                        reference.remove(reference.keySet().iterator().next());
                    }
                } else {
                    check(Objects.equals(cache.get(key), reference.get(key)), "Lookup differs from reference");
                }
                List<Integer> expected = new ArrayList<>(reference.keySet());
                Collections.reverse(expected);
                check(cache.size() == reference.size(), "Size differs from reference");
                check(cache.keysMostRecentFirst().equals(expected), "Recency differs from reference");
            }
        }
    }

    private static void concurrentAccess() throws Exception {
        int workers = 8;
        LRUCache<Integer, Integer> retained = new LRUCache<>(workers);
        LRUCache<Integer, Integer> shared = new LRUCache<>(16);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int worker = 0; worker < workers; worker++) {
                final int key = worker;
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < 2000; i++) {
                        retained.put(key, i);
                        check(retained.get(key) == i, "Concurrent access lost a retained entry");
                        shared.put((i + key) % 64, i);
                        shared.get((i * 3 + key) % 64);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) future.get(15, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers failed to stop");
        }
        check(retained.size() == workers, "Concurrent inserts lost entries");
        List<Integer> keys = shared.keysMostRecentFirst();
        check(keys.size() == 16 && new HashSet<>(keys).size() == 16, "List must match bounded map");
        for (Integer key : keys) check(shared.get(key) != null, "List contains a missing map entry");
        for (int key = 100; key < 116; key++) shared.put(key, key);
        for (Integer key : keys) check(shared.get(key) == null, "Old entries must all be evicted");
        check(shared.size() == 16, "Cache exceeded its capacity");
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

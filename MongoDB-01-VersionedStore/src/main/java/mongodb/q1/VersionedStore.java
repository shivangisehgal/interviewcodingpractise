package mongodb.q1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Append-only point-in-time store; writes per key must increase in timestamp. */
public final class VersionedStore {
    private record Version(long timestamp, String value, boolean deleted) { }
    private final Map<String, List<Version>> histories = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void set(String key, String value, long timestamp) {
        append(key, new Version(timestamp, Objects.requireNonNull(value, "value"), false));
    }

    public void delete(String key, long timestamp) {
        append(key, new Version(timestamp, null, true));
    }

    private void append(String key, Version version) {
        Objects.requireNonNull(key, "key");
        lock.writeLock().lock();
        try {
            List<Version> history = histories.computeIfAbsent(key, ignored -> new ArrayList<>());
            if (!history.isEmpty() && version.timestamp() <= history.get(history.size() - 1).timestamp()) {
                throw new IllegalArgumentException("Timestamps must strictly increase per key");
            }
            history.add(version);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(String key, long timestamp) {
        Objects.requireNonNull(key, "key");
        lock.readLock().lock();
        try {
            List<Version> history = histories.get(key);
            if (history == null) return null;
            int low = 0, high = history.size();
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (history.get(mid).timestamp() <= timestamp) low = mid + 1;
                else high = mid;
            }
            if (low == 0) return null;
            Version version = history.get(low - 1);
            return version.deleted() ? null : version.value();
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void main(String[] args) {
        VersionedStore store = new VersionedStore();
        store.set("user:1", "Alice", 5);
        store.delete("user:1", 10);
        store.set("user:1", "Bob", 15);
        for (long time : new long[] {3, 7, 12, 20}) {
            System.out.println("get(" + time + ") = " + store.get("user:1", time));
        }
    }
}

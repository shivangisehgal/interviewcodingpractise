package mongodb.q3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class OplogReplay {
    public enum Operation { INSERT, UPDATE, DELETE }
    public record Entry(long timestamp, Operation operation, String key, String value) {
        public Entry {
            Objects.requireNonNull(operation, "operation");
            Objects.requireNonNull(key, "key");
            if (operation != Operation.DELETE) Objects.requireNonNull(value, "value");
        }
    }
    public record Result(Map<String, String> store, long resumeTimestamp) {
        public Result { store = Map.copyOf(store); }
    }
    private OplogReplay() { }

    /** Input is already timestamp-ordered. */
    public static Map<String, String> replay(List<Entry> entries) {
        Objects.requireNonNull(entries, "entries");
        Map<String, String> store = new HashMap<>();
        Long previous = null;
        for (Entry entry : entries) {
            if (previous != null && entry.timestamp() < previous) {
                throw new IllegalArgumentException("Replay expects timestamp-ordered entries");
            }
            apply(store, entry);
            previous = entry.timestamp();
        }
        return Map.copyOf(store);
    }

    /** Returns a new snapshot; does not mutate the supplied store or entries. */
    public static Result replayFrom(Map<String, String> initial, List<Entry> entries, long resumeTimestamp) {
        Objects.requireNonNull(initial, "initial");
        Objects.requireNonNull(entries, "entries");
        Map<String, String> store = new HashMap<>(Map.copyOf(initial));
        TreeMap<Long, Entry> fresh = new TreeMap<>();
        for (Entry entry : entries) {
            if (entry.timestamp() > resumeTimestamp) fresh.putIfAbsent(entry.timestamp(), entry);
        }
        fresh.values().forEach(entry -> apply(store, entry));
        long next = fresh.isEmpty() ? resumeTimestamp : fresh.lastKey();
        return new Result(store, next);
    }

    private static void apply(Map<String, String> store, Entry entry) {
        switch (entry.operation()) {
            case INSERT -> store.put(entry.key(), entry.value());
            case UPDATE -> {
                if (store.containsKey(entry.key())) store.put(entry.key(), entry.value());
            }
            case DELETE -> store.remove(entry.key());
        }
    }

    public static void main(String[] args) {
        List<Entry> entries = List.of(new Entry(120, Operation.UPDATE, "a", "9"),
                new Entry(110, Operation.INSERT, "c", "3"),
                new Entry(120, Operation.UPDATE, "a", "9"),
                new Entry(130, Operation.DELETE, "b", null));
        Result result = replayFrom(Map.of("a", "1", "b", "2"), entries, 100);
        System.out.println(new TreeMap<>(result.store()));
        System.out.println("Resume timestamp: " + result.resumeTimestamp());
    }
}

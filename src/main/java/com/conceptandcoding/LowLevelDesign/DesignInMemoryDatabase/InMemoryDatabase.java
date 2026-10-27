package com.conceptandcoding.LowLevelDesign.DesignInMemoryDatabase;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Single-session key/value database with nested, LIFO transactions. */
public final class InMemoryDatabase<K, V> {
    private record PreviousValue<V>(boolean existed, V value) { }

    private final Map<K, V> data = new HashMap<>();
    private final Deque<Map<K, PreviousValue<V>>> transactions = new ArrayDeque<>();

    /** A stored null is returned normally; an absent key throws. */
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        if (!data.containsKey(key)) throw new NoSuchElementException("Key not found: " + key);
        return data.get(key);
    }

    public void set(K key, V value) {
        Objects.requireNonNull(key, "key");
        rememberOriginal(key);
        data.put(key, value);
    }

    /** Unsetting a missing key is a no-op. */
    public void unset(K key) {
        Objects.requireNonNull(key, "key");
        if (data.containsKey(key)) {
            rememberOriginal(key);
            data.remove(key);
        }
    }

    public boolean containsKey(K key) {
        return data.containsKey(Objects.requireNonNull(key, "key"));
    }

    public int size() { return data.size(); }
    public int transactionDepth() { return transactions.size(); }

    public void begin() {
        transactions.push(new HashMap<>());
    }

    /** Commits only the innermost transaction; the parent can still abort it. */
    public void commit() {
        requireTransaction();
        Map<K, PreviousValue<V>> changes = transactions.pop();
        if (!transactions.isEmpty()) {
            Map<K, PreviousValue<V>> parent = transactions.peek();
            // The parent's older before-image wins if both levels touched a key.
            changes.forEach(parent::putIfAbsent);
        }
    }

    /** Restores the state immediately before the innermost begin(). */
    public void abort() {
        requireTransaction();
        Map<K, PreviousValue<V>> changes = transactions.pop();
        changes.forEach((key, previous) -> {
            if (previous.existed()) data.put(key, previous.value());
            else data.remove(key);
        });
    }

    private void rememberOriginal(K key) {
        if (!transactions.isEmpty()) {
            transactions.peek().computeIfAbsent(key,
                    ignored -> new PreviousValue<>(data.containsKey(key), data.get(key)));
        }
    }

    private void requireTransaction() {
        if (transactions.isEmpty()) throw new IllegalStateException("No active transaction");
    }
}

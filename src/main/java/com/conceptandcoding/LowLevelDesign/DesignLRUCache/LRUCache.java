package com.conceptandcoding.LowLevelDesign.DesignLRUCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Fixed-capacity, thread-safe LRU cache with average O(1) get and put. */
public final class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> entries = new HashMap<>();
    private final DoublyLinkedList<K, V> recency = new DoublyLinkedList<>();

    public LRUCache(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
    }

    /** Returns null on a miss; a hit makes the entry most recently used. */
    public synchronized V get(K key) {
        Node<K, V> entry = entries.get(Objects.requireNonNull(key, "key"));
        if (entry == null) return null;
        recency.promote(entry);
        return entry.value;
    }

    /** Inserts or updates an entry, counting either operation as usage. */
    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> entry = entries.get(key);
        if (entry != null) {
            entry.value = value;
            recency.promote(entry);
            return;
        }
        if (entries.size() == capacity) {
            Node<K, V> victim = recency.evictOldest();
            entries.remove(victim.key);
        }
        Node<K, V> added = new Node<>(key, value);
        entries.put(key, added);
        recency.prepend(added);
    }

    public synchronized int size() { return entries.size(); }

    /** Immutable O(n) diagnostic snapshot; does not affect recency. */
    public synchronized List<K> keysMostRecentFirst() {
        return recency.keysMostRecentFirst();
    }
}

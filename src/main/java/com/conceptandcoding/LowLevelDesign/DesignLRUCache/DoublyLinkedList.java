package com.conceptandcoding.LowLevelDesign.DesignLRUCache;

import java.util.ArrayList;
import java.util.List;

/** Used only while holding the owning cache's monitor. */
final class DoublyLinkedList<K, V> {
    private final Node<K, V> newest = new Node<>(null, null);
    private final Node<K, V> oldest = new Node<>(null, null);

    DoublyLinkedList() {
        newest.next = oldest;
        oldest.previous = newest;
    }

    void prepend(Node<K, V> entry) {
        Node<K, V> first = newest.next;
        entry.previous = newest;
        entry.next = first;
        first.previous = entry;
        newest.next = entry;
    }

    private void unlink(Node<K, V> entry) {
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
        entry.previous = null;
        entry.next = null;
    }

    void promote(Node<K, V> entry) {
        unlink(entry);
        prepend(entry);
    }

    Node<K, V> evictOldest() {
        Node<K, V> victim = oldest.previous;
        if (victim == newest) return null;
        unlink(victim);
        return victim;
    }

    List<K> keysMostRecentFirst() {
        List<K> keys = new ArrayList<>();
        for (Node<K, V> entry = newest.next; entry != oldest; entry = entry.next) {
            keys.add(entry.key);
        }
        return List.copyOf(keys);
    }
}

package com.conceptandcoding.LowLevelDesign.DesignLRUCache;

/** Internal entry shared by the lookup map and recency list. */
final class Node<K, V> {
    final K key;
    V value;
    Node<K, V> previous;
    Node<K, V> next;

    Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

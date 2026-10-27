package com.conceptandcoding.LowLevelDesign.DesignLRUCache;

public final class LRUCacheDemo {
    public static void main(String[] args) {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        System.out.println("Get 1: " + cache.get(1));
        cache.put(4, "D");
        System.out.println("Get 2 (evicted): " + cache.get(2));
        System.out.println("MRU to LRU: " + cache.keysMostRecentFirst());
        cache.put(1, "Updated A");
        System.out.println("Updated 1: " + cache.get(1));
        System.out.println("MRU to LRU: " + cache.keysMostRecentFirst());
    }
}

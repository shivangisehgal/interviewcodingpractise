# Design LRU Cache

A Java 17 implementation of the core design described in
[AlgoMaster: Design LRU Cache](https://algomaster.io/learn/lld/design-lru-cache).
This is an adaptation written for this repository, with explicit input validation
and standalone tests.

## Structure and behavior

- `LRUCache<K, V>` exposes `get`, `put`, `size`, and a diagnostic recency snapshot.
- A hash map finds entries in average O(1) time. `DoublyLinkedList` maintains
  recency using sentinel nodes and constant-time pointer changes.
- `Node` holds a key, value and links. Keeping the key permits map removal
  during eviction without scanning.
- A successful `get` or any `put` makes that entry most recently used. Inserting
  a new key at capacity removes the least recently used entry.
- A missing key returns `null`. Null keys and values are rejected, so a miss
  is unambiguous. Capacity must be positive; zero and negative capacities throw
  `IllegalArgumentException`.
- All public operations synchronize on the same cache instance, keeping the map
  and list consistent. Even `get` needs exclusive access because it changes recency.
- `keysMostRecentFirst()` returns an immutable O(n) snapshot without changing order.

`get` and `put` take average O(1) time excluding lock contention; total storage is
O(capacity). Keys must retain stable hash codes and equality while cached. Values
are returned by reference; synchronization protects cache state, not mutable values.
The core implementation uses strict global LRU. TTL, weighted eviction, metrics
and striped caches discussed as extensions in the article are not implemented.

## Compile and run from the repository root

```sh
mkdir -p target/lru-cache
javac -d target/lru-cache src/main/java/com/conceptandcoding/LowLevelDesign/DesignLRUCache/*.java
java -cp target/lru-cache com.conceptandcoding.LowLevelDesign.DesignLRUCache.LRUCacheDemo
```

Expected output:

```text
Get 1: A
Get 2 (evicted): null
MRU to LRU: [4, 1, 3]
Updated 1: Updated A
MRU to LRU: [1, 4, 3]
```

## Run tests

```sh
javac -cp target/lru-cache -d target/lru-cache src/test/java/com/conceptandcoding/LowLevelDesign/DesignLRUCache/LRUCacheTest.java
java -cp target/lru-cache com.conceptandcoding.LowLevelDesign.DesignLRUCache.LRUCacheTest
```

Tests cover read/update recency, misses, eviction, capacity one, invalid inputs,
immutable snapshots, 40,000 seeded operations checked against an access-ordered
`LinkedHashMap`, and concurrent readers/writers with and without eviction.
No Spring context or external dependencies are required.

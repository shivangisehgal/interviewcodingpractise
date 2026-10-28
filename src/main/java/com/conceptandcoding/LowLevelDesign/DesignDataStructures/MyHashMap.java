
import java.util.Objects;

public class MyHashMap<K, V> {

    private static final int INITIAL_CAPACITY = 1 << 4; // 16
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] hashTable;
    private int size;
    private int threshold;

    // Each bucket contains a linked list of entries.
    private static class Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Entry<K, V> next;

        Entry(int hash, K key, V value, Entry<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // Default constructor: capacity 16
    public MyHashMap() {
        this(INITIAL_CAPACITY);
    }

    // Custom capacity constructor
    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative capacity");
        }

        int tableSize = tableSizeFor(capacity);

        hashTable = (Entry<K, V>[]) new Entry[tableSize];
        threshold = calculateThreshold(tableSize);
        size = 0;
    }

    // Round capacity to next power of 2
    private static int tableSizeFor(int cap) {
        int n = cap - 1;

        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;

        return (n < 0) ? 1 :
               (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private static int calculateThreshold(int capacity) {
        if (capacity == MAXIMUM_CAPACITY) {
            return Integer.MAX_VALUE;
        }
        return (int) (capacity * LOAD_FACTOR);
    }

    // Mix higher hash bits into lower bits
    private int hash(K key) {
        if (key == null) {
            return 0;
        }

        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    // Capacity is always power of 2
    private int getIndex(int hash) {
        return hash & (hashTable.length - 1);
    }

    // INSERT OR UPDATE
    public V put(K key, V value) {

        int h = hash(key);
        int index = getIndex(h);

        Entry<K, V> curr = hashTable[index];

        // Check whether key already exists
        while (curr != null) {

            if (curr.hash == h &&
                Objects.equals(curr.key, key)) {

                V oldValue = curr.value;
                curr.value = value;
                return oldValue;
            }

            curr = curr.next;
        }

        // New key: insert at head in O(1)
        Entry<K, V> newNode =
                new Entry<>(h, key, value, hashTable[index]);

        hashTable[index] = newNode;
        size++;

        // Resize only when a new key increases size
        if (size > threshold) {
            resize();
        }

        return null;
    }

    // RETRIEVE
    public V get(K key) {

        Entry<K, V> node = findNode(key);

        return node == null ? null : node.value;
    }

    // CHECK KEY EXISTENCE
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    // Shared helper for get and containsKey
    private Entry<K, V> findNode(K key) {

        int h = hash(key);
        int index = getIndex(h);

        Entry<K, V> curr = hashTable[index];

        while (curr != null) {

            if (curr.hash == h &&
                Objects.equals(curr.key, key)) {

                return curr;
            }

            curr = curr.next;
        }

        return null;
    }

    // REMOVE
    public V remove(K key) {

        int h = hash(key);
        int index = getIndex(h);

        Entry<K, V> curr = hashTable[index];
        Entry<K, V> prev = null;

        while (curr != null) {

            if (curr.hash == h &&
                Objects.equals(curr.key, key)) {

                // Removing first node
                if (prev == null) {
                    hashTable[index] = curr.next;
                }
                // Removing middle or last node
                else {
                    prev.next = curr.next;
                }

                size--;
                return curr.value;
            }

            prev = curr;
            curr = curr.next;
        }

        return null;
    }

    // DOUBLE CAPACITY AND REDISTRIBUTE ENTRIES
    @SuppressWarnings("unchecked")
    private void resize() {

        int oldCapacity = hashTable.length;

        // Do not exceed maximum capacity
        if (oldCapacity >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        int newCapacity = oldCapacity << 1;

        Entry<K, V>[] newTable =
                (Entry<K, V>[]) new Entry[newCapacity];

        // Iterate through all old buckets
        for (int i = 0; i < oldCapacity; i++) {

            Entry<K, V> curr = hashTable[i];

            while (curr != null) {

                // Save next before changing links
                Entry<K, V> nextNode = curr.next;

                // Recompute bucket index
                int newIndex = curr.hash & (newCapacity - 1);

                // Insert at head of new bucket
                curr.next = newTable[newIndex];
                newTable[newIndex] = curr;

                // Move to next old node
                curr = nextNode;
            }
        }

        hashTable = newTable;
        threshold = calculateThreshold(newCapacity);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return hashTable.length;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        hashTable = (Entry<K, V>[]) new Entry[hashTable.length];
        size = 0;
    }

    // TEST
    public static void main(String[] args) {

        MyHashMap<Integer, String> map = new MyHashMap<>(7);

        System.out.println(map.capacity()); // 8

        map.put(1, "hi");
        map.put(2, "my");
        map.put(3, "name");
        map.put(4, "is");
        map.put(5, "Shrayansh");
        map.put(6, "how");

        System.out.println(map.capacity()); // 8

        map.put(7, "are"); // Size 7 > threshold 6

        System.out.println(map.capacity()); // 16

        map.put(8, "you");
        map.put(9, "friends");
        map.put(10, "?");

        System.out.println(map.get(8)); // you
        System.out.println(map.get(9)); // friends

        map.put(8, "everyone");
        System.out.println(map.get(8)); // everyone

        System.out.println(map.remove(8)); // everyone
        System.out.println(map.containsKey(8)); // false

        map.put(null, "null key");
        System.out.println(map.get(null)); // null key

        System.out.println(map.size()); // 10

        map.clear();
        System.out.println(map.isEmpty()); // true
    }
}

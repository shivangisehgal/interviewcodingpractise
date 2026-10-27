# Design Bounded Blocking Queue

A Java 17 adaptation of [Design Bounded Blocking Queue by codeWithAryan](https://codewitharyan.com/tech-blogs/design-bounded-blocking-queue).

## Behavior and synchronization

- `BoundedBlockingQueue(int capacity)` requires a positive maximum capacity.
- `enqueue(int element)` waits when full, then inserts at the front.
- `dequeue()` waits when empty, then removes the oldest element from the rear.
- `size()` returns the current count without waiting for space or an item.
- Two semaphores count available slots and available items, as in the article.
  Blocking semaphore acquisition happens outside the storage monitor so the
  opposite operation can proceed. An interrupted semaphore wait throws
  `InterruptedException` without consuming a permit or changing the queue.
- This adaptation uses an `ArrayDeque` protected by a private monitor. Its
  constant-time size makes `size()` an exact synchronized snapshot, addressing
  the article's incorrect claim that `ConcurrentLinkedDeque.size()` is O(1).
- If insertion/removal fails before completing, its reserved permit is restored.

The queue preserves element FIFO order; scheduling order between simultaneous
producers or consumers is unspecified. Semaphores are nonfair. Enqueue/dequeue
can wait indefinitely until the opposite operation occurs or the waiting thread
is interrupted. There is no timeout or close API. Interruptibility applies to the
semaphore wait; acquiring the storage monitor is not interruptible.

Deque insertion is amortized O(1); removal and size are O(1), excluding waits.
Space is O(capacity). Every integer value is accepted, including negative values.

## Compile and run from the repository root

```sh
mkdir -p target/bounded-queue
javac -d target/bounded-queue src/main/java/com/conceptandcoding/LowLevelDesign/DesignBoundedBlockingQueue/*.java
java -cp target/bounded-queue com.conceptandcoding.LowLevelDesign.DesignBoundedBlockingQueue.Main
```

Expected output:

```text
Consumed in FIFO order: [1, 2, 3, 4, 5]
Final size: 0
```

## Focused tests

```sh
javac -cp target/bounded-queue -d target/bounded-queue src/test/java/com/conceptandcoding/LowLevelDesign/DesignBoundedBlockingQueue/BoundedBlockingQueueTest.java
java -cp target/bounded-queue com.conceptandcoding.LowLevelDesign.DesignBoundedBlockingQueue.BoundedBlockingQueueTest
```

Tests cover FIFO and size, capacity validation, blocked producer/consumer wakeup,
interruption without permit loss, and concurrent producers/consumers with no lost
or duplicated elements. Tests and demo need only Java 17.

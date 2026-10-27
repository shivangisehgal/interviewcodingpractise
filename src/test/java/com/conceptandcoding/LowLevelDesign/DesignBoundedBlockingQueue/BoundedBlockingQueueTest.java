package com.conceptandcoding.LowLevelDesign.DesignBoundedBlockingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public final class BoundedBlockingQueueTest {
    public static void main(String[] args) throws Exception {
        for (int capacity : new int[] {0, -1}) {
            try {
                new BoundedBlockingQueue(capacity);
                throw new AssertionError("Invalid capacity accepted");
            } catch (IllegalArgumentException expected) { }
        }
        BoundedBlockingQueue queue = new BoundedBlockingQueue(3);
        queue.enqueue(-1);
        queue.enqueue(0);
        queue.enqueue(42);
        check(queue.size() == 3, "Size must reflect enqueued elements");
        check(queue.dequeue() == -1 && queue.dequeue() == 0 && queue.dequeue() == 42, "FIFO violated");
        check(queue.size() == 0, "Drained queue must be empty");
        blockingAndInterruption(true, false);
        blockingAndInterruption(false, false);
        blockingAndInterruption(true, true);
        blockingAndInterruption(false, true);
        concurrentTransfer();
        System.out.println("Bounded blocking queue tests passed");
    }

    private static void blockingAndInterruption(boolean producer, boolean interrupt) throws Exception {
        BoundedBlockingQueue queue = new BoundedBlockingQueue(1);
        if (producer) queue.enqueue(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicReference<Integer> received = new AtomicReference<>();
        Thread blocked = new Thread(() -> {
            try {
                if (producer) queue.enqueue(2);
                else received.set(queue.dequeue());
            } catch (InterruptedException expected) {
                interrupted.set(true);
            } catch (Throwable unexpected) {
                failure.set(unexpected);
            }
        });
        blocked.setDaemon(true);
        blocked.start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (blocked.getState() != Thread.State.WAITING && blocked.isAlive()
                    && System.nanoTime() < deadline) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            }
            check(blocked.getState() == Thread.State.WAITING, "Operation must block on unavailable permit");
            check(queue.size() == (producer ? 1 : 0), "Blocked operation changed size");
            if (interrupt) blocked.interrupt();
            else if (producer) check(queue.dequeue() == 1, "Wrong initial element");
            else queue.enqueue(7);
            blocked.join(3000);
            check(!blocked.isAlive(), "Blocked operation failed to wake");
            check(failure.get() == null, "Unexpected worker failure: " + failure.get());
            check(interrupted.get() == interrupt, "Incorrect interruption result");
            if (interrupt) {
                check(queue.size() == (producer ? 1 : 0), "Interrupted operation mutated queue");
                if (producer) check(queue.dequeue() == 1, "Interrupted producer changed stored value");
                queue.enqueue(9);
                check(queue.dequeue() == 9 && queue.size() == 0, "Interruption leaked a permit");
            } else if (producer) {
                check(queue.size() == 1 && queue.dequeue() == 2, "Producer failed to insert after wakeup");
            } else {
                check(received.get() == 7 && queue.size() == 0, "Consumer failed to receive after wakeup");
            }
        } finally {
            blocked.interrupt();
            blocked.join(3000);
        }
    }

    private static void concurrentTransfer() throws Exception {
        int workers = 4;
        int perWorker = 1000;
        BoundedBlockingQueue queue = new BoundedBlockingQueue(7);
        Set<Integer> received = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(workers * 2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        try {
            for (int worker = 0; worker < workers; worker++) {
                final int offset = worker * perWorker;
                results.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < perWorker; i++) {
                        queue.enqueue(offset + i);
                        check(queue.size() <= 7, "Capacity exceeded");
                    }
                    return null;
                }));
                results.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < perWorker; i++) {
                        check(received.add(queue.dequeue()), "Element delivered twice");
                        check(queue.size() >= 0, "Negative size");
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> result : results) result.get(10, TimeUnit.SECONDS);
            check(queue.size() == 0 && received.size() == workers * perWorker, "Elements lost");
            for (int value = 0; value < workers * perWorker; value++) {
                check(received.contains(value), "Missing value " + value);
            }
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers did not stop");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

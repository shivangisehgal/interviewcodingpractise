package com.conceptandcoding.LowLevelDesign.DesignBoundedBlockingQueue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;

/** FIFO integer queue with interruptible waits for space and elements. */
public final class BoundedBlockingQueue {
    private final Semaphore availableSlots;
    private final Semaphore availableItems = new Semaphore(0);
    private final Deque<Integer> elements = new ArrayDeque<>();

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        availableSlots = new Semaphore(capacity);
    }

    public void enqueue(int element) throws InterruptedException {
        // Wait outside the monitor so consumers can make room.
        availableSlots.acquire();
        boolean inserted = false;
        try {
            synchronized (elements) {
                elements.addFirst(element);
                inserted = true;
                availableItems.release();
            }
        } finally {
            if (!inserted) availableSlots.release();
        }
    }

    public int dequeue() throws InterruptedException {
        availableItems.acquire();
        boolean removed = false;
        try {
            synchronized (elements) {
                int element = elements.removeLast();
                removed = true;
                availableSlots.release();
                return element;
            }
        } finally {
            if (!removed) availableItems.release();
        }
    }

    /** Exact at the instant the monitor is held; may change after returning. */
    public int size() {
        synchronized (elements) {
            return elements.size();
        }
    }
}

package com.conceptandcoding.LowLevelDesign.DesignBoundedBlockingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Main {
    public static void main(String[] args) throws Exception {
        BoundedBlockingQueue queue = new BoundedBlockingQueue(2);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<?> producer = workers.submit(() -> {
                for (int value = 1; value <= 5; value++) queue.enqueue(value);
                return null;
            });
            Future<List<Integer>> consumer = workers.submit(() -> {
                List<Integer> received = new ArrayList<>();
                for (int i = 0; i < 5; i++) received.add(queue.dequeue());
                return received;
            });
            producer.get(5, TimeUnit.SECONDS);
            System.out.println("Consumed in FIFO order: " + consumer.get(5, TimeUnit.SECONDS));
            System.out.println("Final size: " + queue.size());
        } finally {
            workers.shutdownNow();
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Workers did not stop");
            }
        }
    }
}

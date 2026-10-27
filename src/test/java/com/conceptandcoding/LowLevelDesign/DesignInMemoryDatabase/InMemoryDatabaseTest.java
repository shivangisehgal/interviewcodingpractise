package com.conceptandcoding.LowLevelDesign.DesignInMemoryDatabase;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/** Standalone tests requiring only Java 17. */
public final class InMemoryDatabaseTest {
    public static void main(String[] args) {
        InMemoryDatabase<String, Integer> db = new InMemoryDatabase<>();
        expect(NoSuchElementException.class, () -> db.get("missing"));
        db.unset("missing");
        expect(IllegalStateException.class, db::commit);
        expect(IllegalStateException.class, db::abort);
        expect(NullPointerException.class, () -> db.set(null, 1));
        expect(NullPointerException.class, () -> db.get(null));
        expect(NullPointerException.class, () -> db.unset(null));
        expect(NullPointerException.class, () -> db.containsKey(null));
        db.set("x", 10);
        db.set("nullable", null);
        db.begin();
        db.set("x", 20);
        db.set("x", 21);
        db.unset("nullable");
        db.set("created", null);
        db.abort();
        check(db.get("x") == 10, "Repeated writes must restore the original value");
        check(db.containsKey("nullable") && db.get("nullable") == null, "Null value not restored");
        check(!db.containsKey("created"), "Created null key must disappear after abort");

        db.begin();
        db.set("x", 30);
        db.begin();
        db.set("x", 40);
        db.unset("nullable");
        db.set("child", 7);
        db.commit();
        check(db.get("x") == 40 && db.transactionDepth() == 1, "Child commit must retain working values");
        db.abort();
        check(db.get("x") == 10, "Child commit overwrote the parent's original value");
        check(db.containsKey("nullable") && db.get("nullable") == null, "Child deletion lost prior null");
        check(!db.containsKey("child"), "Parent abort must undo child-created keys");

        db.begin();
        db.unset("x");
        db.begin();
        db.set("x", 50);
        db.abort();
        check(!db.containsKey("x"), "Child abort must retain parent's deletion");
        db.abort();
        check(db.get("x") == 10, "Parent abort must restore deleted key");
        db.begin();
        db.set("new", 1);
        db.begin();
        db.unset("new");
        db.set("new", 2);
        db.commit();
        db.abort();
        check(!db.containsKey("new"), "Nested recreation must preserve original absence");
        db.begin();
        db.begin();
        db.commit();
        db.abort();
        check(db.transactionDepth() == 0, "Empty transactions must close normally");
        db.begin();
        db.set("x", null);
        db.commit();
        check(db.containsKey("x") && db.get("x") == null, "Outermost commit must persist null");
        expect(IllegalStateException.class, db::abort);
        compareWithReference();
        System.out.println("In-memory database tests passed");
    }

    private static void compareWithReference() {
        InMemoryDatabase<Integer, Integer> db = new InMemoryDatabase<>();
        Map<Integer, Integer> expected = new HashMap<>();
        Deque<Map<Integer, Integer>> snapshots = new ArrayDeque<>();
        Random random = new Random(20261027);
        for (int step = 0; step < 20000; step++) {
            int key = random.nextInt(16);
            switch (random.nextInt(7)) {
                case 0, 1 -> {
                    Integer value = random.nextInt(4) == 0 ? null : random.nextInt(1000);
                    db.set(key, value);
                    expected.put(key, value);
                }
                case 2 -> { db.unset(key); expected.remove(key); }
                case 3 -> {
                    if (snapshots.size() < 10) {
                        snapshots.push(new HashMap<>(expected));
                        db.begin();
                    }
                }
                case 4 -> {
                    if (snapshots.isEmpty()) expect(IllegalStateException.class, db::commit);
                    else { snapshots.pop(); db.commit(); }
                }
                case 5 -> {
                    if (snapshots.isEmpty()) expect(IllegalStateException.class, db::abort);
                    else { expected = snapshots.pop(); db.abort(); }
                }
                default -> { /* Inspect without mutation below. */ }
            }
            check(db.size() == expected.size(), "Size differs from reference");
            check(db.transactionDepth() == snapshots.size(), "Depth differs from reference");
            for (int candidate = 0; candidate < 16; candidate++) {
                final int inspected = candidate;
                check(db.containsKey(candidate) == expected.containsKey(candidate), "Existence differs");
                if (expected.containsKey(candidate)) {
                    check(Objects.equals(db.get(candidate), expected.get(candidate)), "Value differs");
                } else {
                    expect(NoSuchElementException.class, () -> db.get(inspected));
                }
            }
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

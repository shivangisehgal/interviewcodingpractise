package mongodb.q3;

import java.util.*;
import mongodb.q3.OplogReplay.*;

public final class SolutionTest {
    public static void main(String[] args) {
        List<Entry> ordered = List.of(new Entry(1, Operation.INSERT, "a", "1"),
                new Entry(2, Operation.UPDATE, "missing", "ignored"), new Entry(3, Operation.UPDATE, "a", "2"),
                new Entry(4, Operation.DELETE, "a", null), new Entry(5, Operation.DELETE, "missing", null),
                new Entry(6, Operation.INSERT, "a", "3"));
        check(OplogReplay.replay(ordered).equals(Map.of("a", "3")), "Replay semantics");
        check(OplogReplay.replay(List.of()).isEmpty(), "Empty replay");
        expect(IllegalArgumentException.class, () -> OplogReplay.replay(List.of(ordered.get(5), ordered.get(0))));
        Map<String, String> initial = new HashMap<>(Map.of("a", "1", "b", "2"));
        List<Entry> messy = List.of(new Entry(90, Operation.INSERT, "a", "stale"),
                new Entry(120, Operation.UPDATE, "a", "9"), new Entry(110, Operation.INSERT, "c", "3"),
                new Entry(120, Operation.UPDATE, "a", "conflicting-duplicate"), new Entry(130, Operation.DELETE, "b", null));
        Result result = OplogReplay.replayFrom(initial, messy, 100);
        check(result.store().equals(Map.of("a", "9", "c", "3")) && result.resumeTimestamp() == 130, "Resumable replay");
        check(initial.equals(Map.of("a", "1", "b", "2")), "Input store mutated");
        check(OplogReplay.replayFrom(result.store(), messy, 130).equals(result), "Replay must be idempotent");
        check(OplogReplay.replayFrom(initial, List.of(), 100).resumeTimestamp() == 100, "Empty token changed");
        Result noOp = OplogReplay.replayFrom(Map.of(), List.of(new Entry(10, Operation.UPDATE, "absent", "x")), 0);
        check(noOp.store().isEmpty() && noOp.resumeTimestamp() == 10, "No-op must still advance token");
        Random random = new Random(3);
        List<Entry> log = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            log.add(new Entry(i, Operation.values()[random.nextInt(3)], "k" + random.nextInt(20), "v" + i));
        }
        Map<String, String> reference = OplogReplay.replay(log);
        List<Entry> delivered = new ArrayList<>(log);
        delivered.addAll(log); Collections.shuffle(delivered, random);
        Result all = OplogReplay.replayFrom(Map.of(), delivered, 0);
        check(all.store().equals(reference) && all.resumeTimestamp() == 1000, "Messy replay differs from clean log");
        Map<String, String> halfway = OplogReplay.replay(log.subList(0, 500));
        check(OplogReplay.replayFrom(halfway, delivered, 500).store().equals(reference), "Resumption differs");
        expect(UnsupportedOperationException.class, () -> result.store().put("x", "y"));
        System.out.println("MongoDB-03 tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}

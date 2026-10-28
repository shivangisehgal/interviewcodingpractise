package mongodb.q4;

import java.util.*;
import mongodb.q4.DocumentMatcher.*;

public final class SolutionTest {
    public static void main(String[] args) {
        List<Map<String, Long>> docs = List.of(Map.of("id", 1L, "age", 25L, "score", 80L, "plan", 2L),
                Map.of("id", 2L, "age", 17L, "score", 95L, "plan", 1L),
                Map.of("id", 3L, "age", 40L, "score", 60L, "plan", 2L),
                Map.of("id", 4L, "age", 30L, "score", 80L));
        check(ids(DocumentMatcher.findEqual(docs, Map.of("plan", 2L))).equals(List.of(1L, 3L)), "Equality");
        check(ids(DocumentMatcher.findEqual(docs, Map.of("plan", 2L, "score", 80L))).equals(List.of(1L)), "Implicit AND");
        check(DocumentMatcher.findEqual(docs, Map.of()).equals(docs), "Empty query");
        check(ids(DocumentMatcher.findMatches(docs, List.of(new Condition("age", Operator.GTE, List.of(18L)),
                new Condition("age", Operator.LT, List.of(40L))))).equals(List.of(1L, 4L)), "Range");
        check(ids(DocumentMatcher.findMatches(docs, List.of(new Condition("plan", Operator.NE, List.of(2L)))))
                .equals(List.of(2L, 4L)), "NE must match missing field");
        check(ids(DocumentMatcher.findMatches(docs, List.of(new Condition("plan", Operator.IN, List.of(1L, 2L)))))
                .equals(List.of(1L, 2L, 3L)), "IN membership");
        check(DocumentMatcher.findMatches(docs, List.of(new Condition("age", Operator.IN, List.of()))).isEmpty(), "Empty IN");
        for (Operator op : Operator.values()) {
            var absent = DocumentMatcher.findMatches(List.of(Map.of()), List.of(new Condition("x", op, List.of(1L))));
            check(absent.size() == (op == Operator.NE ? 1 : 0), "Missing field for " + op);
        }
        Random random = new Random(4);
        for (int i = 0; i < 2000; i++) {
            long a = i == 0 ? Long.MIN_VALUE : random.nextInt(20) - 10;
            long b = i == 0 ? Long.MAX_VALUE : random.nextInt(20) - 10;
            for (Operator op : Operator.values()) {
                boolean expected = switch (op) {
                    case EQ, IN -> a == b; case NE -> a != b; case GT -> a > b;
                    case GTE -> a >= b; case LT -> a < b; case LTE -> a <= b;
                };
                boolean actual = !DocumentMatcher.findMatches(List.of(Map.of("x", a)),
                        List.of(new Condition("x", op, List.of(b)))).isEmpty();
                check(actual == expected, "Operator mismatch " + op);
            }
        }
        expect(IllegalArgumentException.class, () -> new Condition("age", Operator.EQ, List.of()));
        expect(IllegalArgumentException.class, () -> new Condition("age", Operator.GT, List.of(1L, 2L)));
        System.out.println("MongoDB-04 tests passed");
    }
    private static List<Long> ids(List<Map<String, Long>> documents) {
        return documents.stream().map(doc -> doc.get("id")).toList();
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

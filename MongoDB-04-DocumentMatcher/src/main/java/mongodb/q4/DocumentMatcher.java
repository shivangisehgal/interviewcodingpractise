package mongodb.q4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Numeric document matching; no JSON parser or external dependencies. */
public final class DocumentMatcher {
    public enum Operator { EQ, NE, GT, GTE, LT, LTE, IN }
    public record Condition(String field, Operator operator, List<Long> operands) {
        public Condition {
            Objects.requireNonNull(field, "field");
            Objects.requireNonNull(operator, "operator");
            operands = List.copyOf(operands);
            if (operator != Operator.IN && operands.size() != 1) {
                throw new IllegalArgumentException("Comparison requires exactly one operand");
            }
        }
    }
    private DocumentMatcher() { }

    public static List<Map<String, Long>> findEqual(List<Map<String, Long>> documents, Map<String, Long> query) {
        List<Condition> conditions = new ArrayList<>();
        query.forEach((field, value) -> conditions.add(new Condition(field, Operator.EQ, List.of(value))));
        return findMatches(documents, conditions);
    }

    public static List<Map<String, Long>> findMatches(List<Map<String, Long>> documents,
                                                     List<Condition> conditions) {
        Objects.requireNonNull(documents, "documents");
        List<Condition> validated = List.copyOf(conditions);
        List<Map<String, Long>> result = new ArrayList<>();
        for (Map<String, Long> document : documents) {
            Map<String, Long> candidate = Objects.requireNonNull(document, "document");
            boolean matches = true;
            for (Condition condition : validated) {
                if (!matches(candidate, condition)) { matches = false; break; }
            }
            if (matches) result.add(candidate);
        }
        return List.copyOf(result);
    }

    private static boolean matches(Map<String, Long> document, Condition condition) {
        if (!document.containsKey(condition.field())) return condition.operator() == Operator.NE;
        long value = document.get(condition.field());
        if (condition.operator() == Operator.IN) return condition.operands().contains(value);
        long operand = condition.operands().get(0);
        return switch (condition.operator()) {
            case EQ -> value == operand;
            case NE -> value != operand;
            case GT -> value > operand;
            case GTE -> value >= operand;
            case LT -> value < operand;
            case LTE -> value <= operand;
            case IN -> throw new AssertionError("IN already handled");
        };
    }

    public static void main(String[] args) {
        List<Map<String, Long>> docs = List.of(Map.of("id", 1L, "age", 25L),
                Map.of("id", 2L, "age", 17L), Map.of("id", 3L, "age", 40L));
        System.out.println(findMatches(docs, List.of(new Condition("age", Operator.GTE, List.of(18L)),
                new Condition("age", Operator.LT, List.of(40L)))));
    }
}

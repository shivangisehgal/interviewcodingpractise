package mongodb.q2;

import java.util.*;

public final class SolutionTest {
    public static void main(String[] args) {
        String text = "# export\r\n\r\n Name: John\r\nEMAIL : UPPER@EXAMPLE.COM\r\nurl: https://a:b\r\n"
                + "junk\r\nname: Final\r\n : ignored\r\nempty: \r\n\r\n# comment\r\nID: 2";
        var records = LegacyDataCleanup.parseRecords(text);
        check(records.size() == 2, "Record boundaries or final flush");
        check(records.get(0).equals(Map.of("name", "Final", "email", "UPPER@EXAMPLE.COM",
                "url", "https://a:b", "empty", "")), "Normalization or first-colon split");
        check(records.get(1).equals(Map.of("id", "2")), "Final record lost");
        check(LegacyDataCleanup.parseRecords("\n# comment\njunk\n\n").isEmpty(), "Empty record emitted");
        var result = LegacyDataCleanup.mergeFiles(List.of(
                "id: 1\nname: John\nemail: old\nversion: 1\n\nid: 2\nname: Jane",
                "id: 1\nemail: new\nphone: 555\nversion: 2\n\nname: no-id"));
        check(result.equals(List.of(Map.of("id", "1", "name", "John", "email", "new", "phone", "555"),
                Map.of("id", "2", "name", "Jane"))), "Merge example");
        result = LegacyDataCleanup.mergeFiles(List.of("id: 1\na: oldest\nversion: 1",
                "id: 1\nb: newest\nversion: 10", "id: 1\na: middle\nversion: 5",
                "id: 1\na: tie-wins\nversion: 5", "id: 1\nb: stale\nc: backfill\nversion: -1"));
        check(result.get(0).equals(Map.of("id", "1", "a", "tie-wins", "b", "newest", "c", "backfill")),
                "Versions must be per field, ties by input order, gaps always filled");
        check(LegacyDataCleanup.mergeFiles(List.of("id: \nname: skipped")).isEmpty(), "Blank id");
        expect(NumberFormatException.class, () -> LegacyDataCleanup.mergeFiles(List.of("id: 1\nversion: bad")));
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            check(LegacyDataCleanup.parseRecords("ID: 1").get(0).containsKey("id"), "Locale-dependent keys");
        } finally { Locale.setDefault(original); }
        Random random = new Random(2);
        List<String> files = new ArrayList<>();
        Map<String, Map<String, Long>> versions = new HashMap<>();
        Map<String, Map<String, String>> values = new LinkedHashMap<>();
        for (int i = 0; i < 1000; i++) {
            String id = "" + random.nextInt(10), field = "f" + random.nextInt(5), value = "v" + i;
            long version = random.nextInt(20) - 10;
            files.add("id: " + id + "\n" + field + ": " + value + "\nversion: " + version);
            var v = versions.computeIfAbsent(id, ignored -> new HashMap<>());
            var document = values.computeIfAbsent(id, ignored -> new LinkedHashMap<>(Map.of("id", id)));
            if (!v.containsKey(field) || version >= v.get(field)) { v.put(field, version); document.put(field, value); }
        }
        check(LegacyDataCleanup.mergeFiles(files).equals(new ArrayList<>(values.values())), "Reference merge mismatch");
        System.out.println("MongoDB-02 tests passed");
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

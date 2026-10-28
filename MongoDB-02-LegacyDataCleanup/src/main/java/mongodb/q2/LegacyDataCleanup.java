package mongodb.q2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class LegacyDataCleanup {
    private record FieldValue(String value, long version) { }
    private LegacyDataCleanup() { }

    public static List<Map<String, String>> parseRecords(String content) {
        Objects.requireNonNull(content, "content");
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> current = new LinkedHashMap<>();
        for (String raw : content.split("\\R", -1)) {
            String line = raw.strip();
            if (line.isEmpty()) {
                if (!current.isEmpty()) {
                    records.add(current);
                    current = new LinkedHashMap<>();
                }
            } else if (!line.startsWith("#")) {
                int colon = line.indexOf(':');
                if (colon < 0) continue;
                String key = line.substring(0, colon).strip().toLowerCase(Locale.ROOT);
                if (!key.isEmpty()) current.put(key, line.substring(colon + 1).strip());
            }
        }
        if (!current.isEmpty()) records.add(current);
        return records;
    }

    /** Higher version wins per field; equal versions favor later input records. */
    public static List<Map<String, String>> mergeFiles(List<String> contents) {
        Objects.requireNonNull(contents, "contents");
        Map<String, Map<String, FieldValue>> customers = new LinkedHashMap<>();
        for (String content : contents) {
            for (Map<String, String> record : parseRecords(content)) {
                String id = record.get("id");
                if (id == null || id.isBlank()) continue;
                long version = Long.parseLong(record.getOrDefault("version", "0"));
                Map<String, FieldValue> fields = customers.computeIfAbsent(id, ignored -> new LinkedHashMap<>());
                record.forEach((key, value) -> {
                    if (key.equals("id") || key.equals("version")) return;
                    FieldValue previous = fields.get(key);
                    if (previous == null || version >= previous.version()) {
                        fields.put(key, new FieldValue(value, version));
                    }
                });
            }
        }
        List<Map<String, String>> result = new ArrayList<>();
        customers.forEach((id, fields) -> {
            Map<String, String> record = new LinkedHashMap<>();
            record.put("id", id);
            fields.forEach((key, value) -> record.put(key, value.value()));
            result.add(record);
        });
        return result;
    }

    public static void main(String[] args) {
        String first = "id: 1\nname: John\nemail: old@example.com\nversion: 1";
        String second = "id: 1\nemail: new@example.com\nphone: 555-1234\nversion: 2";
        System.out.println(mergeFiles(List.of(first, second)));
    }
}

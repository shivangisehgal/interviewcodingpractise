package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.util.stream.Collectors;

public final class JsonLogFormatter implements LogFormatter {
    @Override
    public String format(LogMessage message) {
        String context = message.context().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> quote(entry.getKey()) + ":" + quote(entry.getValue()))
                .collect(Collectors.joining(","));
        return "{\"timestamp\":" + quote(message.timestamp().toString())
                + ",\"level\":" + quote(message.level().name())
                + ",\"message\":" + quote(message.message())
                + ",\"context\":{" + context + "}}";
    }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) result.append(String.format("\\u%04x", (int) c));
                    else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }
}

package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;
import java.time.Instant;

public final class JsonFormatter implements LogFormatter {
    @Override
    public String format(LogMessage message) {
        return "{\"timestamp\":" + quote(Instant.ofEpochMilli(message.getTimestamp()).toString())
                + ",\"level\":" + quote(message.getLevel().name())
                + ",\"message\":" + quote(message.getMessage()) + "}";
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

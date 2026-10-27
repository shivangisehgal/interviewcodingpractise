package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record LogMessage(Instant timestamp, LogLevel level, String message,
                         Map<String, String> context) {
    public LogMessage {
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(level);
        Objects.requireNonNull(message);
        context = Map.copyOf(context);
    }
}

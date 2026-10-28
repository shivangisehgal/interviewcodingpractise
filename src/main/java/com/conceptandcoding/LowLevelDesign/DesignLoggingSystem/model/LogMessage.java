package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import java.util.Objects;

public final class LogMessage {
    private final LogLevel level;
    private final String message;
    private final long timestamp;

    public LogMessage(LogLevel level, String message, long timestamp) {
        this.level = Objects.requireNonNull(level, "level");
        this.message = Objects.requireNonNull(message, "message");
        this.timestamp = timestamp;
    }

    public LogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}

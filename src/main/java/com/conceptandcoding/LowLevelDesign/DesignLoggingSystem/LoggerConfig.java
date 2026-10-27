package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.util.Objects;

public record LoggerConfig(LogLevel minimumLevel, LogHandler handlers) {
    public LoggerConfig {
        Objects.requireNonNull(minimumLevel);
        Objects.requireNonNull(handlers);
    }
}

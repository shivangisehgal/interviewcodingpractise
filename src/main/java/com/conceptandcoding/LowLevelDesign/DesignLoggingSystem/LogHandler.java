package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.IOException;
import java.util.Objects;

/** An immutable chain node: each matching destination receives the entry once. */
public final class LogHandler {
    private final LogLevel minimumLevel;
    private final LogFormatter formatter;
    private final LogAppender appender;
    private final LogHandler next;

    public LogHandler(LogLevel minimumLevel, LogFormatter formatter,
                      LogAppender appender, LogHandler next) {
        this.minimumLevel = Objects.requireNonNull(minimumLevel);
        this.formatter = Objects.requireNonNull(formatter);
        this.appender = Objects.requireNonNull(appender);
        this.next = next;
    }

    public void handle(LogMessage message) throws IOException {
        if (minimumLevel.accepts(message.level())) {
            appender.append(formatter.format(message));
        }
        if (next != null) next.handle(message);
    }
}

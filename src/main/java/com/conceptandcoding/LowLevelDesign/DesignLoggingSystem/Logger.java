package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class Logger {
    private volatile LoggerConfig config = new LoggerConfig(LogLevel.INFO,
            new LogHandler(LogLevel.DEBUG, new SimpleLogFormatter(), new ConsoleAppender(), null));

    private Logger() { }

    private static class Holder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() { return Holder.INSTANCE; }

    public void setConfig(LoggerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public void log(LogLevel level, String message) throws IOException {
        log(level, message, Map.of());
    }

    public void log(LogLevel level, String message, Map<String, String> context) throws IOException {
        Objects.requireNonNull(level);
        LoggerConfig snapshot = config;
        if (snapshot.minimumLevel().accepts(level)) {
            snapshot.handlers().handle(new LogMessage(Instant.now(), level, message, context));
        }
    }

    public void debug(String message) throws IOException { log(LogLevel.DEBUG, message); }
    public void info(String message) throws IOException { log(LogLevel.INFO, message); }
    public void warning(String message) throws IOException { log(LogLevel.WARNING, message); }
    public void error(String message) throws IOException { log(LogLevel.ERROR, message); }
    public void fatal(String message) throws IOException { log(LogLevel.FATAL, message); }
}

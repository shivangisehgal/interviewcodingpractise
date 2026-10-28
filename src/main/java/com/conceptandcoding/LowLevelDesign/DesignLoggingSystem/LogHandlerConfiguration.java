package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders.LogAppender;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.handlers.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class LogHandlerConfiguration {
    private static final Map<LogLevel, LogHandler> HANDLERS = initialize();

    private LogHandlerConfiguration() { }

    private static Map<LogLevel, LogHandler> initialize() {
        Map<LogLevel, LogHandler> handlers = new EnumMap<>(LogLevel.class);
        handlers.put(LogLevel.TRACE, new TraceHandler());
        handlers.put(LogLevel.DEBUG, new DebugHandler());
        handlers.put(LogLevel.INFO, new InfoHandler());
        handlers.put(LogLevel.WARN, new WarnHandler());
        handlers.put(LogLevel.ERROR, new ErrorHandler());
        handlers.put(LogLevel.FATAL, new FatalHandler());
        LogHandler previous = null;
        for (LogLevel level : LogLevel.values()) {
            LogHandler handler = handlers.get(level);
            if (previous != null) previous.setNext(handler);
            previous = handler;
        }
        return Map.copyOf(handlers);
    }

    /** Returns the shared chain without rewiring it on subsequent calls. */
    public static LogHandler build() { return HANDLERS.get(LogLevel.TRACE); }

    public static void addAppenderForLevel(LogLevel level, LogAppender appender) {
        HANDLERS.get(Objects.requireNonNull(level, "level")).subscribe(appender);
    }

    public static void removeAppenderForLevel(LogLevel level, LogAppender appender) {
        HANDLERS.get(Objects.requireNonNull(level, "level")).unsubscribe(appender);
    }
}

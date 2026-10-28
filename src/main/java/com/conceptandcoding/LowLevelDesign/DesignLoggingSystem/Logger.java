package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.handlers.*;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;

public final class Logger {
    private static final Logger INSTANCE = new Logger();

    private final LogHandler handlerChain;

    private Logger() {
        handlerChain = LogHandlerConfiguration.build();
    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void log(LogLevel level, String message) {
        LogMessage msg = new LogMessage(level, message, System.currentTimeMillis());
        handlerChain.handle(msg);
    }

    public void trace(String msg) { log(LogLevel.TRACE, msg); }
    public void debug(String msg) { log(LogLevel.DEBUG, msg); }
    public void info(String msg)  { log(LogLevel.INFO, msg); }
    public void warn(String msg)  { log(LogLevel.WARN, msg); }
    public void error(String msg) { log(LogLevel.ERROR, msg); }
    public void fatal(String msg) { log(LogLevel.FATAL, msg); }
}

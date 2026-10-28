package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders.ConsoleAppender;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders.FileAppender;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter.JsonFormatter;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter.PlainTextFormatter;

public final class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        ConsoleAppender console = new ConsoleAppender(new PlainTextFormatter());
        String path = args.length == 0 ? "logging-system.log" : args[0];
        try (FileAppender file = new FileAppender(new JsonFormatter(), path)) {
            LogHandlerConfiguration.addAppenderForLevel(LogLevel.INFO, console);
            LogHandlerConfiguration.addAppenderForLevel(LogLevel.ERROR, console);
            LogHandlerConfiguration.addAppenderForLevel(LogLevel.ERROR, file);
            try {
                logger.info("Application started");
                logger.error("Payment failed: \"declined\"");
            } finally {
                LogHandlerConfiguration.removeAppenderForLevel(LogLevel.INFO, console);
                LogHandlerConfiguration.removeAppenderForLevel(LogLevel.ERROR, console);
                LogHandlerConfiguration.removeAppenderForLevel(LogLevel.ERROR, file);
            }
        }
    }
}

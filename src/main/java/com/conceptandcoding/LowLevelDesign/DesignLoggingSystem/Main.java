package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.nio.file.Path;
import java.util.Map;

public final class Main {
    public static void main(String[] args) throws Exception {
        Path logFile = Path.of(args.length == 0 ? "logging-system.log" : args[0]);
        LogHandler errorsToFile = new LogHandler(LogLevel.ERROR, new JsonLogFormatter(),
                new FileAppender(logFile), null);
        LogHandler console = new LogHandler(LogLevel.DEBUG, new SimpleLogFormatter(),
                new ConsoleAppender(), errorsToFile);
        Logger logger = Logger.getInstance();
        logger.setConfig(new LoggerConfig(LogLevel.INFO, console));
        logger.debug("Filtered at the INFO threshold");
        logger.info("Application started");
        logger.log(LogLevel.ERROR, "Payment declined", Map.of("requestId", "req-42"));
        logger.setConfig(new LoggerConfig(LogLevel.DEBUG, console));
        logger.debug("Debug logging enabled at runtime");
    }
}

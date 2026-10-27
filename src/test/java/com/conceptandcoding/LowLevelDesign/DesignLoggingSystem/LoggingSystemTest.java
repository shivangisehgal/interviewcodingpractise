package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Run directly with Java 17; no Spring context or third-party dependencies needed. */
public final class LoggingSystemTest {
    public static void main(String[] args) throws Exception {
        List<String> console = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        LogFormatter formatter = new SimpleLogFormatter();
        LogHandler chain = new LogHandler(LogLevel.DEBUG, formatter, console::add,
                new LogHandler(LogLevel.ERROR, formatter, errors::add, null));
        Logger logger = Logger.getInstance();
        check(logger == Logger.getInstance(), "Logger must be a singleton");
        logger.setConfig(new LoggerConfig(LogLevel.INFO, chain));
        for (LogLevel level : LogLevel.values()) logger.log(level, level.name());
        check(console.size() == 4, "INFO threshold must suppress DEBUG only");
        check(errors.size() == 2, "Error destination must receive ERROR and FATAL once each");
        logger.setConfig(new LoggerConfig(LogLevel.DEBUG, chain));
        logger.debug("enabled");
        check(console.size() == 5 && errors.size() == 2, "Runtime configuration must apply");

        Map<String, String> context = new HashMap<>();
        context.put("request", "original");
        LogMessage entry = new LogMessage(Instant.EPOCH, LogLevel.ERROR, "quote\"\n\\\t\u0001", context);
        context.put("request", "changed");
        check(entry.context().get("request").equals("original"), "Context must be copied");
        String json = new JsonLogFormatter().format(entry);
        check(json.equals("{\"timestamp\":\"1970-01-01T00:00:00Z\",\"level\":\"ERROR\","
                + "\"message\":\"quote\\\"\\n\\\\\\t\\u0001\",\"context\":{\"request\":\"original\"}}"),
                "JSON must escape quotes, slashes and control characters");
        LogMessage escapedContext = new LogMessage(Instant.EPOCH, LogLevel.INFO, "ok",
                Map.of("a\"", "b\n"));
        check(new JsonLogFormatter().format(escapedContext).contains("\"a\\\"\":\"b\\n\""),
                "Context keys and values must be escaped");

        Path file = Files.createTempFile("logging-system-", ".log");
        try {
            FileAppender appender = new FileAppender(file);
            appender.append(json);
            appender.append("second");
            check(Files.readAllLines(file).equals(List.of(json, "second")), "File output must append");
        } finally {
            Files.deleteIfExists(file);
        }
        LogHandler failing = new LogHandler(LogLevel.DEBUG, formatter,
                message -> { throw new IOException("Destination unavailable"); }, null);
        try {
            failing.handle(entry);
            throw new AssertionError("Appender failure must be reported");
        } catch (IOException expected) {
            check(expected.getMessage().equals("Destination unavailable"), "Preserve I/O failure");
        }
        System.out.println("Logging system tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

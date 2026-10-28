package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders.*;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter.*;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Standalone tests requiring only Java 17. */
public final class LoggingSystemTest {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getInstance();
        check(logger == Logger.getInstance(), "Logger must be a singleton");
        check(LogHandlerConfiguration.build() == LogHandlerConfiguration.build(), "Chain must be shared");
        Map<LogLevel, AtomicInteger> counts = new EnumMap<>(LogLevel.class);
        Map<LogLevel, LogAppender> appenders = new EnumMap<>(LogLevel.class);
        for (LogLevel level : LogLevel.values()) {
            AtomicInteger count = new AtomicInteger();
            counts.put(level, count);
            LogAppender appender = message -> {
                check(message.getLevel() == level, "Message routed to wrong severity");
                count.incrementAndGet();
            };
            appenders.put(level, appender);
            LogHandlerConfiguration.addAppenderForLevel(level, appender);
            LogHandlerConfiguration.addAppenderForLevel(level, appender);
        }
        AtomicInteger secondError = new AtomicInteger();
        LogAppender extra = message -> secondError.incrementAndGet();
        LogHandlerConfiguration.addAppenderForLevel(LogLevel.ERROR, extra);
        try {
            for (LogLevel level : LogLevel.values()) logger.log(level, level.name());
            counts.forEach((level, count) -> check(count.get() == 1, "Exact-level routing or deduplication failed"));
            check(secondError.get() == 1, "Multiple appenders must receive the same message");
            logger.trace("trace"); logger.debug("debug"); logger.info("info");
            logger.warn("warn"); logger.error("error"); logger.fatal("fatal");
            counts.forEach((level, count) -> check(count.get() == 2, "Convenience method failed"));
            LogHandlerConfiguration.removeAppenderForLevel(LogLevel.INFO, appenders.get(LogLevel.INFO));
            logger.info("unsubscribed");
            check(counts.get(LogLevel.INFO).get() == 2, "Removed appender still received messages");
            LogHandlerConfiguration.addAppenderForLevel(LogLevel.INFO, appenders.get(LogLevel.INFO));
            concurrentLogs(logger);
            check(counts.get(LogLevel.INFO).get() == 4002, "Concurrent messages lost or duplicated");
            expect(NullPointerException.class, () -> logger.log(null, "invalid"));
            expect(NullPointerException.class, () -> logger.info(null));
            expect(NullPointerException.class, () -> LogHandlerConfiguration.addAppenderForLevel(LogLevel.INFO, null));
        } finally {
            appenders.forEach(LogHandlerConfiguration::removeAppenderForLevel);
            LogHandlerConfiguration.removeAppenderForLevel(LogLevel.ERROR, extra);
        }
        logger.error("No subscriptions should produce no output");
        check(secondError.get() == 2, "Appender cleanup failed");

        LogMessage entry = new LogMessage(LogLevel.ERROR, "quote\"\n\\\t\u0001世界", 0);
        String json = new JsonFormatter().format(entry);
        check(json.equals("{\"timestamp\":\"1970-01-01T00:00:00Z\",\"level\":\"ERROR\","
                + "\"message\":\"quote\\\"\\n\\\\\\t\\u0001世界\"}"), "Invalid JSON escaping");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            new ConsoleAppender(new PlainTextFormatter(), output).append(entry);
        }
        check(bytes.toString(StandardCharsets.UTF_8).contains("[ERROR] - quote"), "Console formatting failed");
        Path path = Files.createTempFile("logger-update-", ".log");
        try {
            FileAppender closed;
            try (FileAppender file = new FileAppender(new JsonFormatter(), path.toString())) {
                file.append(entry);
                closed = file;
            }
            expect(UncheckedIOException.class, () -> closed.append(entry));
            try (FileAppender file = new FileAppender(new JsonFormatter(), path.toString())) {
                file.append(entry);
            }
            check(Files.readAllLines(path).equals(List.of(json, json)), "UTF-8 file append or close failed");
        } finally {
            Files.deleteIfExists(path);
        }
        System.out.println("Logging system tests passed");
    }

    private static void concurrentLogs(Logger logger) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) futures.add(pool.submit(() -> {
                start.await();
                for (int j = 0; j < 500; j++) logger.info("concurrent");
                return null;
            }));
            start.countDown();
            for (Future<?> future : futures) future.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "Workers did not stop");
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

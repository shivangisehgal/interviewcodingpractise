package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter.LogFormatter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class FileAppender implements LogAppender, AutoCloseable {
    private final LogFormatter formatter;
    private final BufferedWriter writer;

    public FileAppender(LogFormatter formatter, String fileName) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        try {
            writer = Files.newBufferedWriter(Path.of(fileName), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException failure) {
            throw new UncheckedIOException("Failed to open log file", failure);
        }
    }

    @Override
    public synchronized void append(LogMessage message) {
        try {
            writer.write(formatter.format(message));
            writer.newLine();
            writer.flush();
        } catch (IOException failure) {
            throw new UncheckedIOException("Failed to write log file", failure);
        }
    }

    @Override
    public synchronized void close() {
        try { writer.close(); }
        catch (IOException failure) { throw new UncheckedIOException("Failed to close log file", failure); }
    }
}

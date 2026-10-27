package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class FileAppender implements LogAppender {
    private final Path path;

    public FileAppender(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public synchronized void append(String formattedMessage) throws IOException {
        Files.writeString(path, formattedMessage + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}

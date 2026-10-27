package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.PrintStream;
import java.util.Objects;

public final class ConsoleAppender implements LogAppender {
    private final PrintStream output;

    public ConsoleAppender() { this(System.out); }

    public ConsoleAppender(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void append(String formattedMessage) {
        output.println(formattedMessage);
    }
}

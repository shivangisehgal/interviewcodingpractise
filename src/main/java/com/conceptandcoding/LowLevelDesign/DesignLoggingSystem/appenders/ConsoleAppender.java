package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter.LogFormatter;
import java.io.PrintStream;
import java.util.Objects;

public final class ConsoleAppender implements LogAppender {
    private final LogFormatter formatter;
    private final PrintStream output;

    public ConsoleAppender(LogFormatter formatter) { this(formatter, System.out); }

    public ConsoleAppender(LogFormatter formatter, PrintStream output) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public void append(LogMessage message) { output.println(formatter.format(message)); }
}

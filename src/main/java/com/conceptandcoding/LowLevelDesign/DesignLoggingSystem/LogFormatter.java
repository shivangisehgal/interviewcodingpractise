package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

@FunctionalInterface
public interface LogFormatter {
    String format(LogMessage message);
}

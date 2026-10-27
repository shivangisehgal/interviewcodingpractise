package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

public enum LogLevel {
    DEBUG, INFO, WARNING, ERROR, FATAL;

    public boolean accepts(LogLevel level) {
        return level.compareTo(this) >= 0;
    }
}

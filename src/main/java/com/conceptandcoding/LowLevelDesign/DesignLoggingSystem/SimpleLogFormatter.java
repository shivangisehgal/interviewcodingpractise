package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

public final class SimpleLogFormatter implements LogFormatter {
    @Override
    public String format(LogMessage message) {
        return message.timestamp() + " [" + message.level() + "] " + message.message()
                + (message.context().isEmpty() ? "" : " " + message.context());
    }
}

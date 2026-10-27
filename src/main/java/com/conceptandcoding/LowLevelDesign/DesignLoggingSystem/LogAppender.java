package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem;

import java.io.IOException;

@FunctionalInterface
public interface LogAppender {
    void append(String formattedMessage) throws IOException;
}

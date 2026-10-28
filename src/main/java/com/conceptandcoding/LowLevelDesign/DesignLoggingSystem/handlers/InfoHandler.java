package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.handlers;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;

public class InfoHandler extends LogHandler {
    @Override
    protected boolean canHandle(LogLevel level) {
        return level == LogLevel.INFO;
    }
}

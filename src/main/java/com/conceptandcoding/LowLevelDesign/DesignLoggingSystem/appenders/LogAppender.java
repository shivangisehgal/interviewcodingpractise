package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;

public interface LogAppender {
    void append(LogMessage message);
}

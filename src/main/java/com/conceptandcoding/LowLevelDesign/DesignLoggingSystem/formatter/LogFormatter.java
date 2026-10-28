package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.formatter;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;

public interface LogFormatter {
    String format(LogMessage message);
}

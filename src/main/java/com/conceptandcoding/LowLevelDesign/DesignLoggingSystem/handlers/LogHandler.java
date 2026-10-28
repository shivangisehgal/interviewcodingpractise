package com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.handlers;

import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.model.LogMessage;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.enums.LogLevel;
import com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.appenders.LogAppender;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class LogHandler {
    protected volatile LogHandler next;
    protected final CopyOnWriteArrayList<LogAppender> appenders = new CopyOnWriteArrayList<>();

    public void setNext(LogHandler next) { this.next = next; }

    public void subscribe(LogAppender observer) {
        appenders.addIfAbsent(Objects.requireNonNull(observer, "observer"));
    }

    public void unsubscribe(LogAppender observer) {
        appenders.remove(Objects.requireNonNull(observer, "observer"));
    }

    public void notifyObservers(LogMessage message) {
        for (LogAppender appender : appenders) appender.append(message);
    }

    public void handle(LogMessage message) {
        Objects.requireNonNull(message, "message");
        if (canHandle(message.getLevel())) notifyObservers(message);
        else if (next != null) next.handle(message);
    }

    protected abstract boolean canHandle(LogLevel level);
}

# Design Logging System

A runnable Java 17 adaptation of the design discussed in
[Design Logging System by codeWithAryan](https://codewitharyan.com/tech-blogs/design-logging-system).
The implementation is written for this repository rather than a verbatim copy of the article snippets.

## Design

- `Logger` is a lazily initialized singleton. Replacing its immutable `LoggerConfig`
  updates the global severity threshold and handler chain atomically; each log call uses one snapshot.
- `LogHandler` forms an immutable chain of responsibility. Every matching destination
  receives the entry once, using its own minimum level, formatter and appender.
- `LogAppender` strategies write to the console or append UTF-8 lines to a file.
- `LogFormatter` strategies produce text or JSON, including escaped message/context fields.
- `LogMessage` stores a timestamp, severity, message and copied string context map.
  Levels are ordered `DEBUG < INFO < WARNING < ERROR < FATAL`.

This adaptation uses enum levels consistently, connects formatters to appenders,
provides the context accessor used by JSON formatting, and avoids duplicate writes
from severity handlers. New destinations implement `LogAppender`.

## Run from the repository root

```sh
mkdir -p target/logging-system
javac -d target/logging-system src/main/java/com/conceptandcoding/LowLevelDesign/DesignLoggingSystem/*.java
java -cp target/logging-system com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.Main
```

The demo writes INFO and higher to the console, ERROR and higher as JSON to
`logging-system.log`, and then enables DEBUG at runtime. An optional first argument
sets the log file path. Its parent directory must already exist.

## Run the focused tests

```sh
javac -cp target/logging-system -d target/logging-system src/test/java/com/conceptandcoding/LowLevelDesign/DesignLoggingSystem/LoggingSystemTest.java
java -cp target/logging-system com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.LoggingSystemTest
```

Tests cover severity filtering, chain routing without duplicate writes, runtime
configuration, singleton identity, context snapshots, JSON escaping, file append
behavior and propagation of destination failures. They require no Spring context.

Logging is synchronous. An I/O failure is passed to the caller and stops the chain;
there is no asynchronous queue, retry, rotation or database destination. File writes
are synchronized per appender instance; share one instance for a given file.

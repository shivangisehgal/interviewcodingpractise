# Design Logging System

Updated from [shubhkpatel/Low-Level-Design: logger](https://github.com/shubhkpatel/Low-Level-Design/tree/fd753ebd1fc03b6a499c8671151f62ed7dc13c05/src/main/java/org/nailyourinterview/lld/logger),
upstream commit `fd753ebd1fc03b6a499c8671151f62ed7dc13c05`. Original implementation credit: shubhkpatel.
This replaces the earlier article-based logging example in this package.

## Design and API

- `Logger` is a public singleton, accessed through `Logger.getInstance()`.
- `LogHandlerConfiguration.build()` returns one shared handler chain:
  `TRACE -> DEBUG -> INFO -> WARN -> ERROR -> FATAL`.
- Each handler matches exactly one severity and notifies its registered appenders.
  This combines Chain of Responsibility with observer-style subscriptions.
- Register using `LogHandlerConfiguration.addAppenderForLevel(level, appender)`;
  unregister using `removeAppenderForLevel`. Re-adding the same appender object
  to a level does not duplicate writes.
- `ConsoleAppender` and `FileAppender` apply their own `LogFormatter` strategy.
  `PlainTextFormatter` uses local timestamps; `JsonFormatter` emits escaped JSON
  with an ISO-8601 UTC timestamp.
- `LogMessage` is immutable and contains severity, message and epoch-millisecond timestamp.
- Convenience methods are `trace`, `debug`, `info`, `warn`, `error` and `fatal`.

Subscriptions are for exact levels: subscribing to ERROR does not receive FATAL.
A level with no subscriptions produces no output. To target multiple levels,
register the appender with each desired level. The old minimum-level `LoggerConfig`,
`setConfig`, context map and `warning` API have been replaced by this source's API;
imports for models, formatters, appenders and levels now use their subpackages.

## Integration fixes

Lombok annotations were replaced with ordinary constructors/getters/setters for
Java 17 compatibility without another dependency. TRACE now has a handler and
convenience method, covering every declared enum value. Chain construction happens
once instead of rewiring shared handlers on repeated builds. Appender lists use
`CopyOnWriteArrayList` for safe subscription changes during logging.

JSON timestamps are quoted and message strings escape quotes, backslashes and
control characters. File output is UTF-8 and synchronized per appender; it flushes
each entry, implements `AutoCloseable`, and reports I/O failures through
`UncheckedIOException`. Unregister an appender before closing it, after stopping
its concurrent producers: an in-progress log call can retain a subscription snapshot.
Use one appender instance per file. Custom formatters/appenders must be safe for
concurrent calls. Appender failures propagate and stop delivery to later appenders.

Logging is synchronous; there is no async queue, log rotation or retry mechanism.

## Compile and run from the repository root

```sh
mkdir -p target/logging-system
find src/main/java/com/conceptandcoding/LowLevelDesign/DesignLoggingSystem -name '*.java' > target/logging-system/sources.txt
javac -d target/logging-system @target/logging-system/sources.txt
java -cp target/logging-system com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.Main
```

The demo sends INFO to the console and ERROR to both the console and a JSON log
file, then unregisters its subscriptions and closes the file. The default output
is `logging-system.log`; an optional first argument changes the path. The parent
directory must exist.

## Focused tests

```sh
javac -cp target/logging-system -d target/logging-system src/test/java/com/conceptandcoding/LowLevelDesign/DesignLoggingSystem/LoggingSystemTest.java
java -cp target/logging-system com.conceptandcoding.LowLevelDesign.DesignLoggingSystem.LoggingSystemTest
```

Tests cover all six levels, exact-level routing, multiple appenders, deduplication,
unsubscription, concurrent logging, input validation, JSON escaping, console output,
UTF-8 file append, close behavior and I/O failure reporting. No Spring context is needed.

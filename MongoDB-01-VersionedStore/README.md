# MongoDB-01: Point-in-Time Versioned Store

Original Java solution based on the multipart requirements in
[BigTechExperts question 1](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-1).
This is an interview exercise; it requires only Java 17 and no MongoDB server.

Covers parts 1–3: timestamped set/get, tombstone deletion, and thread-safe access.

`set(key, value, timestamp)` and `delete(key, timestamp)` append immutable versions.
`get(key, timestamp)` binary-searches the last version at or before the query time.
Unknown keys, prehistory and winning tombstones return null; rewriting after a
tombstone resurrects the key without changing earlier reads.

A global read/write lock protects the history map and lists. Readers share the
read lock; mutations take the write lock. Timestamps must strictly increase per
key **in write-lock acquisition order**; concurrent callers must coordinate that
ordering, otherwise the later-arriving stale write is rejected. A read sees only
writes completed before it acquires its lock; it does not wait for future writes.
Keys/values cannot be null. Signed 64-bit timestamps are supported.

Append is amortized O(1), lookup is O(log v) for v versions of the key, excluding
lock contention. Space is O(w) across all retained writes/tombstones. There is no
compaction, multi-key snapshot API, disk persistence or lock-free MVCC.

Tests cover floor boundaries, history retention, delete/resurrection, invalid
write order, a seeded TreeMap reference comparison, and concurrent readers/writers.

## Compile, test and run from the repository root

```sh
mkdir -p target/mongodb/q1
javac --release 17 -Xlint:all -d target/mongodb/q1 MongoDB-01-VersionedStore/src/main/java/mongodb/q1/*.java MongoDB-01-VersionedStore/src/test/java/mongodb/q1/*.java
java -cp target/mongodb/q1 mongodb.q1.SolutionTest
java -cp target/mongodb/q1 mongodb.q1.VersionedStore
```

Run all five test suites with `bash MongoDB-run-tests.sh`.

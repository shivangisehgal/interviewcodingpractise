# MongoDB-03: Oplog Replay

Original Java solution based on the multipart requirements in
[BigTechExperts question 3](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-3).
This is an interview exercise; it requires only Java 17 and no MongoDB server.

Covers both parts: ordered replay and resumable, deduplicated, out-of-order replay.

`Entry` contains a long timestamp, enum operation, string key and string value.
`INSERT` creates or overwrites; `UPDATE` only changes an existing key; `DELETE`
removes if present. Keys and insert/update values cannot be null. A delete's value
is ignored and may be null. `replay(entries)` validates nondecreasing input order
and returns the final immutable map.

`replayFrom(initial, entries, resumeTimestamp)` skips timestamps at or below the
resume token, keeps the first delivered entry at each newer timestamp, restores
timestamp order with a TreeMap, and applies operations to a copy of the initial
store. `Result` contains an immutable updated store and the highest applied token;
even no-op updates/deletes advance it. No fresh entries leaves the token unchanged.
Conflicting duplicate timestamps use the first entry, following the page's rule.
Inputs are not mutated.

Ordered replay is O(n) expected time and O(k_peak) working space, plus the returned
snapshot. Resumable replay is O(n log u + k) time, O(u + k) space for u distinct
fresh timestamps and k initial/live keys. Null arguments/elements are rejected.

Each call assumes its batch contains every operation needed through its highest
timestamp. A genuinely new older operation arriving in a later batch will be
skipped by the resume rule. This finite-batch exercise does not implement network
gap detection, crash durability or atomic persistence of state and token.

Tests cover missing-key no-ops, deletion/resurrection, redelivery, first-duplicate
wins, immutable outputs, unchanged inputs, no-op token advancement, and seeded
shuffled/duplicated batches compared with clean replay.

## Compile, test and run from the repository root

```sh
mkdir -p target/mongodb/q3
javac --release 17 -Xlint:all -d target/mongodb/q3 MongoDB-03-OplogReplay/src/main/java/mongodb/q3/*.java MongoDB-03-OplogReplay/src/test/java/mongodb/q3/*.java
java -cp target/mongodb/q3 mongodb.q3.SolutionTest
java -cp target/mongodb/q3 mongodb.q3.OplogReplay
```

Run all five test suites with `bash MongoDB-run-tests.sh`.

# MongoDB-05: Fleet Error Report

Original Java solution based on the multipart requirements in
[BigTechExperts question 5](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-5).
This is an interview exercise; it requires only Java 17 and no MongoDB server.

Covers both parts: spread ranking and replica-set blast-radius ranking.

`ServerLog(name, errors)` represents one uniquely named node and all its error
lines, including duplicates. Names use `atlas-<replicaSet>-node-<n>` with no hyphens
inside the replica-set segment and a nonnegative decimal node suffix. Duplicate
node names or malformed names throw `IllegalArgumentException`; null error codes
are rejected. Empty error lists are valid.

`topErrors(fleet, k)` returns `(code, spread, occurrences)`, sorted by distinct
node count descending, total occurrences descending, then code ascending.
`topErrorsByBlastRadius(fleet, k)` returns `(code, worstReplicaSet, nodesHit)`,
sorted by the maximum nodes hit in one replica set descending, global spread
descending, then code ascending. When one error ties across replica sets, the
alphabetically first set is selected. Repeated lines from a single node increase
occurrences, never spread or blast radius. Occurrences use long counters.

k must be nonnegative; zero returns no rows and oversized k returns all codes.
Outputs are immutable. Metrics measure logged error distribution, not proven
node unhealthiness or quorum loss.

For L log lines, S nodes, d codes and H distinct error/node memberships, time is
O(S + L + d log d) plus node-name parsing, and space is O(S + d + H). Both global
and per-set membership sets retain node identities. This includes empty nodes
and avoids undercounting membership storage as only O(d * replicaSets).

Tests cover the six-node example, noisy-node repeats, both spread tie-breaks,
blast/spread/alphabetic ties, worst-set ties, k boundaries, empty fleets, validation,
and shuffled-input determinism.

## Compile, test and run from the repository root

```sh
mkdir -p target/mongodb/q5
javac --release 17 -Xlint:all -d target/mongodb/q5 MongoDB-05-FleetErrorReport/src/main/java/mongodb/q5/*.java MongoDB-05-FleetErrorReport/src/test/java/mongodb/q5/*.java
java -cp target/mongodb/q5 mongodb.q5.SolutionTest
java -cp target/mongodb/q5 mongodb.q5.FleetErrorReport
```

Run all five test suites with `bash MongoDB-run-tests.sh`.

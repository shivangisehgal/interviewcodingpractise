# MongoDB interview Java solutions

Five standalone Java 17 exercises parsed from the linked BigTechExperts pages.
Each includes all numbered parts, a runnable example, focused tests, source
attribution, input assumptions and complexity notes. No external dependencies or
MongoDB connection are needed. These folders are separate from the Spring Boot
application and compile through the commands in each README.

| Folder | Covered parts |
| --- | --- |
| [MongoDB-01-VersionedStore](MongoDB-01-VersionedStore/README.md) | Point-in-Time Versioned Store — point-in-time reads, tombstones, concurrency |
| [MongoDB-02-LegacyDataCleanup](MongoDB-02-LegacyDataCleanup/README.md) | Legacy Data Cleanup — block parsing and per-field versioned merging |
| [MongoDB-03-OplogReplay](MongoDB-03-OplogReplay/README.md) | Oplog Replay — ordered and resumable/idempotent replay |
| [MongoDB-04-DocumentMatcher](MongoDB-04-DocumentMatcher/README.md) | Document Matcher — equality and comparison/membership operators |
| [MongoDB-05-FleetErrorReport](MongoDB-05-FleetErrorReport/README.md) | Fleet Error Report — spread and per-replica-set blast radius |

```sh
bash MongoDB-run-tests.sh
```

The script compiles all five solutions/tests with Java 17 checks and writes only
to ignored `target/mongodb/` build folders. Each solution class also has a `main`
example; see its README for the command.

Folder names carry the requested `MongoDB-` prefix. Java packages use `mongodb.q1`
through `mongodb.q5` because hyphens are not valid Java identifiers.

Q2 deliberately fixes the page's record-wide version tracking to satisfy its
stated newest-value-per-field rule. Q1 callers still supply monotonically ordered
writes per key, and Q3 replay assumes complete batches through their resume token.

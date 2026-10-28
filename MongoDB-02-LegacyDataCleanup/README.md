# MongoDB-02: Legacy Data Cleanup

Original Java solution based on the multipart requirements in
[BigTechExperts question 2](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-2).
This is an interview exercise; it requires only Java 17 and no MongoDB server.

Covers both parts: parse one messy export and merge multiple exports by id/version.

`parseRecords(content)` treats blank lines as record boundaries, skips comments
starting with `#` and malformed lines, splits only on the first colon, lowercases
keys with `Locale.ROOT`, and strips values. Duplicate keys in a record use the
last value. Values such as email addresses are otherwise preserved; empty values
are allowed. The final record is flushed even without a trailing newline.

`mergeFiles(contents)` returns records in first-seen id order. Missing/blank ids
are skipped. Version defaults to zero and must parse as a signed 64-bit integer;
invalid versions throw `NumberFormatException`. The `id` field remains, `version`
is removed. Fields are unioned. Each field's highest version wins; equal versions
favor later input files/records. An absent field can be backfilled at any version.

The article's sample keeps one best version per entire record, which conflicts
with its stated per-field rule. Example: `a@1`, then `b@10`, then `a@5` must yield
`a@5`, not `a@1`. This solution stores a version alongside each field and tests
that regression. Null input strings/lists are rejected. Outputs are fresh mutable
maps and lists.

Expected time is O(N) for N input characters/fields, excluding variable-length
hash costs. Parsing uses O(N) output/split storage; merging keeps O(U) accumulated
fields plus the largest file's parsing storage. This is block text, not CSV/JSON;
it does not handle multiline values or file I/O itself.

Tests cover comments, CRLF, embedded colons, last-record flushing, duplicates,
locale independence, missing ids, versions/ties, backfill, and a seeded merge oracle.

## Compile, test and run from the repository root

```sh
mkdir -p target/mongodb/q2
javac --release 17 -Xlint:all -d target/mongodb/q2 MongoDB-02-LegacyDataCleanup/src/main/java/mongodb/q2/*.java MongoDB-02-LegacyDataCleanup/src/test/java/mongodb/q2/*.java
java -cp target/mongodb/q2 mongodb.q2.SolutionTest
java -cp target/mongodb/q2 mongodb.q2.LegacyDataCleanup
```

Run all five test suites with `bash MongoDB-run-tests.sh`.

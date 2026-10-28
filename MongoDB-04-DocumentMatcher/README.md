# MongoDB-04: Document Matcher

Original Java solution based on the multipart requirements in
[BigTechExperts question 4](https://www.bigtechexperts.com/companies/mongodb/swe-algorithm-questions/interview-question-4).
This is an interview exercise; it requires only Java 17 and no MongoDB server.

Covers both parts: implicit-AND equality and flattened operator conditions.

Documents are `Map<String, Long>`. `findEqual(documents, query)` handles equality
maps. `findMatches(documents, conditions)` accepts immutable `Condition` records
with a field, enum operator and operand list. Operators map to the page's syntax:
`EQ/$eq`, `NE/$ne`, `GT/$gt`, `GTE/$gte`, `LT/$lt`, `LTE/$lte`, `IN/$in`.
Comparisons require exactly one operand; IN accepts zero or more operands.

Conditions use AND, including repeated fields for ranges. A missing field fails
all operators except NE. An empty query/condition list matches every document;
empty IN matches none. Matching documents keep input order. Comparisons use direct
long comparisons, avoiding subtraction overflow. Documents are assumed to contain
only nonnull keys and nonnull long values. Null conditions/operands are rejected.

The result list is immutable but retains the input document references, matching
the source's selection behavior. Callers own those documents and must avoid
concurrent mutation. The matcher handles already-parsed data; it is not a JSON
parser or a general MongoDB query engine.

Time is O(N * (C + total IN operand lengths)) in the worst case for N documents
and C conditions. Additional space is O(C + M) for validated conditions and M
matching references. No indexes or nested/logical operators are implemented.

Tests cover source examples, input order, all operators, absent fields, empty
queries/IN, range conjunctions, operand validation and seeded comparison cases,
including long extremes.

## Compile, test and run from the repository root

```sh
mkdir -p target/mongodb/q4
javac --release 17 -Xlint:all -d target/mongodb/q4 MongoDB-04-DocumentMatcher/src/main/java/mongodb/q4/*.java MongoDB-04-DocumentMatcher/src/test/java/mongodb/q4/*.java
java -cp target/mongodb/q4 mongodb.q4.SolutionTest
java -cp target/mongodb/q4 mongodb.q4.DocumentMatcher
```

Run all five test suites with `bash MongoDB-run-tests.sh`.

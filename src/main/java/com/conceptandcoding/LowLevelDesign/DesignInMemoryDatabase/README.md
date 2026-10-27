# Design In-Memory Database with Transactions

A Java 17 implementation based on the requirements in
[Hello Interview: Design an in-memory database to handle transactions](https://www.hellointerview.com/community/questions/memory-database-transactions/cm6uaoy1y00003b6la4jojd7s).
The Java implementation is written for this repository.

## API and semantics

- `set(key, value)` stores a value, including `null`.
- `get(key)` returns its current value, or throws `NoSuchElementException` if absent.
- `unset(key)` removes the key; removing an absent key does nothing.
- `begin()` opens an empty nested transaction.
- `commit()` closes only the innermost transaction. Inner changes remain visible,
  but an enclosing transaction can still abort them. An outermost commit makes
  changes permanent within this in-memory session.
- `abort()` undoes only the innermost transaction.
- Commit/abort without a transaction throws `IllegalStateException`.
- Null keys are rejected. `containsKey`, `size` and `transactionDepth` provide
  nonmutating inspection of current state.

## Undo-log design

A hash map stores the current working view. Each transaction holds only the
before-image of each key it first changes, including an explicit existence flag.
This distinguishes a stored null from a previously absent key. Repeated changes
within one level never replace that level's original value.

On child commit, before-images merge into the parent only for keys the parent
has not already tracked. This preserves the earliest value for an outer abort.
On abort, existing keys regain their values and newly created keys are removed.
This addresses the null/absence ambiguity and parent-log overwriting in the
page's illustrative snippets.

`get`, `set`, `unset`, `containsKey`, and `begin` take average/amortized O(1)
time; size/depth inspection is O(1). Commit and abort take O(k) for the distinct
keys changed at that level (merging a child on commit or restoring on abort).
The page's blanket O(1) requirement does not apply to these log-processing steps.
Storage is O(n + d + sum(k_i)) for current data, transaction depth and active logs;
there is no full-database copy at begin.

This is a single-session, single-threaded interview example. It has no disk
persistence, concurrent-session isolation or crash recovery. Values are stored by
reference; mutating a retrieved object directly bypasses the undo log. Use
immutable values or replace them through `set` for transactional changes. Keys
must retain stable equality/hash codes while stored.

## Compile and run from the repository root

```sh
mkdir -p target/memory-database
javac -d target/memory-database src/main/java/com/conceptandcoding/LowLevelDesign/DesignInMemoryDatabase/*.java
java -cp target/memory-database com.conceptandcoding.LowLevelDesign.DesignInMemoryDatabase.Main
```

Expected output:

```text
During transaction: 20
After abort: 10
After inner commit: 40
After outer abort: 10
Child-created key exists: false
After outer commit: 50
```

## Focused tests

```sh
javac -cp target/memory-database -d target/memory-database src/test/java/com/conceptandcoding/LowLevelDesign/DesignInMemoryDatabase/InMemoryDatabaseTest.java
java -cp target/memory-database com.conceptandcoding.LowLevelDesign.DesignInMemoryDatabase.InMemoryDatabaseTest
```

Tests cover nested commit/abort, repeated writes, creation/deletion rollback,
null values versus absent keys, empty transactions, invalid operations, and
20,000 seeded operations compared with a snapshot-based reference model.

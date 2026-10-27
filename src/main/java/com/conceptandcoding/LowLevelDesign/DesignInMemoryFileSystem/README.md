# Design In-Memory File System

A Java 17 implementation of the requirements in
[AlgoMonster 588: Design In-Memory File System](https://algo.monster/liteproblems/588).
This implementation is written for this repository.

## API and design

- `ls(path)` returns sorted immediate child names for a directory, or a
  single-element list with the name of a file. Missing paths return an empty
  list, matching the linked solution. Returned lists are immutable snapshots.
- `mkdir(path)` creates intermediate directories. Existing directories, including
  root, are left intact.
- `addContentToFile(filePath, content)` creates missing parent directories and
  the file, or appends to an existing file. Empty content creates an empty file.
- `readContentFromFile(filePath)` returns the complete content. A missing file
  throws `NoSuchElementException`.

The hierarchy is a trie of path components: directories map child names to entries,
while files hold a `StringBuilder`. Separate node types prevent converting a
nonempty directory to a file or a file to a directory. Shared path prefixes share
nodes. Names and paths are case-sensitive.

Paths must be canonical absolute strings: `/` is valid, while relative paths,
repeated separators, trailing separators, `.`/`..` components and NUL characters
are rejected with `IllegalArgumentException`. Null paths/content are rejected.
Using a directory as a file or traversing a file as a directory throws
`IllegalStateException`. All path validation occurs before mutation.

This is an in-memory, single-threaded example. It provides no disk access,
permissions, symbolic links, delete/rename operations or concurrent access guarantees.

## Complexity

Let p be path length, n the number of immediate children, a the appended content
length, and c the file's total content length. Hash-map traversal and mkdir are
average O(p). Directory `ls` adds O(n log n) name comparisons for sorting.
Append takes amortized O(p + a), and reading takes O(p + c). Name comparison
costs depend on the string lengths. Storage scales with path nodes/names and file
contents; reads and listings allocate independent results.

## Compile and run from the repository root

```sh
mkdir -p target/file-system
javac -d target/file-system src/main/java/com/conceptandcoding/LowLevelDesign/DesignInMemoryFileSystem/*.java
java -cp target/file-system com.conceptandcoding.LowLevelDesign.DesignInMemoryFileSystem.Main
```

Expected output:

```text
Initial root: []
Root: [a]
Content: hello
After append: hello world
File listing: [d]
Sorted directory: [alpha.txt, d, z]
```

## Focused tests

```sh
javac -cp target/file-system -d target/file-system src/test/java/com/conceptandcoding/LowLevelDesign/DesignInMemoryFileSystem/FileSystemTest.java
java -cp target/file-system com.conceptandcoding.LowLevelDesign.DesignInMemoryFileSystem.FileSystemTest
```

Tests cover the example, shared path prefixes, sorted/file/root listings,
appending and empty files, missing paths, immutable results, invalid paths and
type conflicts. Seeded operations also compare listings and content with a
flat-map reference implementation.

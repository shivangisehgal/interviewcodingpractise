# File System — Kavriq example

Imported and adapted from [Kavriq: Design a File System](https://kavriq.com/interview/lld/v1/file-system/).
Original example credit: Kavriq. Public classes are split into separate Java 17
files and placed in `com.conceptandcoding.LowLevelDesign.DesignFileSystem`.
The demo paths are corrected from `/root` to `/`: the source implementation
names its root object `root` but does not create a child directory named `root`.
A serialization identifier is added to the checked exception.

## Included classes

- `FileSystemEntry` abstracts a named entry with a parent reference.
- `Directory` stores child entries and enforces unique names across files/directories.
- `File` stores content; writing replaces its previous content.
- `FileSystem` creates, navigates, deletes, reads and writes entries.
- `FileSystemController` reports successful operations to stdout and errors to stderr.
- `FileSystemException` represents expected operation failures.
- `FileSystemClient` demonstrates the normal flow and expected error cases.

This Composite-pattern example is separate from `DesignInMemoryFileSystem`, which
implements the earlier AlgoMonster trie API with append semantics. This example
adds explicit parent links, deletion and a controller.

## Source behavior

Paths identify the parent directory; names are supplied separately, for example
`createFile("/docs", "note.txt")`. Parents must already exist. `/` and the empty
path identify root. Traversal ignores empty slash-separated components and starts
at root even for paths without a leading slash. Use simple nonempty entry names
without slashes, `.` or `..`; the source does not validate entry-name syntax.

Listings preserve insertion order and return a copy of the list, containing the
actual entry objects. Deleting a nonempty directory detaches its entire subtree
from root; externally held object references remain usable. Expected errors throw
`FileSystemException` in the model. The controller catches these errors and returns
an empty string/list for failed reads/listings, so these controller results alone
cannot distinguish errors from empty files/directories.

This is a mutable, single-threaded, in-memory teaching example with no disk
persistence, permissions, transaction support or concurrent access guarantees.

## Complexity

Children are stored in an `ArrayList`. Lookup, duplicate checking and removal
are O(n) in the containing directory. Navigation costs the sum of child-list
scans along the path, plus path parsing. Listing copies n child references in
O(n); file reads and replacement writes retain immutable string references.
Storage scales with entries, child references and file contents. These costs
correct the source article's O(1) add/remove description.

## Compile and run from the repository root

```sh
mkdir -p target/kavriq-file-system
javac -d target/kavriq-file-system src/main/java/com/conceptandcoding/LowLevelDesign/DesignFileSystem/*.java
java -cp target/kavriq-file-system com.conceptandcoding.LowLevelDesign.DesignFileSystem.FileSystemClient
```

The demo creates `/docs/note.txt`, writes and reads `Hello, World!`, lists root,
then exercises duplicates, missing files, invalid paths and directory deletion.
The resulting `Error:` messages are intentional demonstrations.

## Focused tests

```sh
javac -cp target/kavriq-file-system -d target/kavriq-file-system src/test/java/com/conceptandcoding/LowLevelDesign/DesignFileSystem/FileSystemTest.java
java -cp target/kavriq-file-system com.conceptandcoding.LowLevelDesign.DesignFileSystem.FileSystemTest
```

Tests cover hierarchy and parent references, content replacement, copied listings,
duplicates/type conflicts, invalid/missing paths, subtree deletion, file recreation
and controller success/error behavior. No external dependencies are required.

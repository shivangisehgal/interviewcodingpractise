package com.conceptandcoding.LowLevelDesign.DesignInMemoryFileSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/** Standalone tests requiring only Java 17. */
public final class FileSystemTest {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        check(fs.ls("/").isEmpty(), "Root should start empty");
        fs.mkdir("/");
        fs.mkdir("/a/b/c");
        fs.mkdir("/a/b/c");
        fs.addContentToFile("/a/b/c/d", "hello");
        check(fs.ls("/").equals(List.of("a")), "Root listing differs");
        check(fs.readContentFromFile("/a/b/c/d").equals("hello"), "File content differs");
        fs.addContentToFile("/a/b/c/d", " world");
        check(fs.readContentFromFile("/a/b/c/d").equals("hello world"), "Append overwrote content");
        check(fs.ls("/a/b/c/d").equals(List.of("d")), "File listing must use basename");
        fs.mkdir("/a/b/z");
        fs.addContentToFile("/a/b/alpha", "");
        check(fs.ls("/a/b").equals(List.of("alpha", "c", "z")), "Children must be sorted");
        check(fs.readContentFromFile("/a/b/alpha").isEmpty(), "Empty file not created");
        fs.addContentToFile("/new/parents/file", "unicode: 世界");
        check(fs.readContentFromFile("/new/parents/file").equals("unicode: 世界"), "Parents not created");
        List<String> snapshot = fs.ls("/");
        expect(UnsupportedOperationException.class, () -> snapshot.add("mutate"));
        fs.mkdir("/later");
        check(!snapshot.contains("later"), "Listing must be an independent snapshot");
        check(fs.ls("/missing/path").isEmpty(), "Missing listing should be empty");
        expect(NoSuchElementException.class, () -> fs.readContentFromFile("/missing/path"));
        check(!fs.ls("/").contains("missing"), "Reads must not create nodes");
        for (String path : List.of("", "relative", "/a/", "//a", "/a//b", "/a/./b", "/a/../b",
                "/invalid" + (char) 0)) {
            expect(IllegalArgumentException.class, () -> fs.mkdir(path));
            expect(IllegalArgumentException.class, () -> fs.addContentToFile(path, "bad"));
            expect(IllegalArgumentException.class, () -> fs.ls(path));
            expect(IllegalArgumentException.class, () -> fs.readContentFromFile(path));
        }
        expect(NullPointerException.class, () -> fs.ls(null));
        expect(NullPointerException.class, () -> fs.addContentToFile("/null/file", null));
        check(!fs.ls("/").contains("null"), "Rejected content must not create parents");
        expect(IllegalArgumentException.class, () -> fs.addContentToFile("/", "bad"));
        expect(IllegalStateException.class, () -> fs.readContentFromFile("/a"));
        expect(IllegalStateException.class, () -> fs.addContentToFile("/a", "bad"));
        expect(IllegalStateException.class, () -> fs.mkdir("/a/b/c/d"));
        expect(IllegalStateException.class, () -> fs.mkdir("/a/b/c/d/child"));
        expect(IllegalStateException.class, () -> fs.addContentToFile("/a/b/c/d/child", "bad"));
        expect(IllegalStateException.class, () -> fs.ls("/a/b/c/d/child"));
        check(fs.readContentFromFile("/a/b/c/d").equals("hello world"), "Conflict damaged existing file");
        compareWithReference();
        System.out.println("In-memory file system tests passed");
    }

    private static void compareWithReference() {
        FileSystem fs = new FileSystem();
        Set<String> directories = new HashSet<>(Set.of("/"));
        Map<String, String> files = new HashMap<>();
        Random random = new Random(588);
        for (int step = 0; step < 5000; step++) {
            String parent = "/d" + random.nextInt(5) + "/s" + random.nextInt(4);
            String file = parent + "/f" + random.nextInt(6);
            if (random.nextInt(3) == 0) {
                String directory = parent + "/extra" + random.nextInt(3);
                fs.mkdir(directory);
                rememberDirectories(directories, directory);
            } else {
                String content = random.nextBoolean() ? "" : "chunk" + step + "世界";
                fs.addContentToFile(file, content);
                files.merge(file, content, String::concat);
                rememberDirectories(directories, parent);
                check(fs.readContentFromFile(file).equals(files.get(file)), "Reference content differs");
                check(fs.ls(file).equals(List.of(file.substring(file.lastIndexOf('/') + 1))),
                        "Reference file listing differs");
            }
            check(fs.ls(parent).equals(expectedListing(parent, directories, files)), "Parent listing differs");
            check(fs.ls("/").equals(expectedListing("/", directories, files)), "Root listing differs");
        }
        for (String directory : directories) {
            check(fs.ls(directory).equals(expectedListing(directory, directories, files)), "Final listing differs");
        }
        files.forEach((path, content) -> check(fs.readContentFromFile(path).equals(content), "Final content differs"));
    }

    private static void rememberDirectories(Set<String> directories, String path) {
        while (!path.isEmpty()) {
            directories.add(path);
            path = path.substring(0, path.lastIndexOf('/'));
        }
    }

    private static List<String> expectedListing(String path, Set<String> directories, Map<String, String> files) {
        Set<String> entries = new HashSet<>(directories);
        entries.addAll(files.keySet());
        String prefix = path.equals("/") ? "/" : path + "/";
        Set<String> children = new TreeSet<>();
        for (String entry : entries) {
            if (entry.startsWith(prefix)) {
                String suffix = entry.substring(prefix.length());
                if (!suffix.isEmpty() && !suffix.contains("/")) children.add(suffix);
            }
        }
        return List.copyOf(children);
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

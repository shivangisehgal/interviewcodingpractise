package com.conceptandcoding.LowLevelDesign.DesignInMemoryFileSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/** In-memory tree of absolute paths; intended for a single-threaded session. */
public final class FileSystem {
    private interface Entry { }

    private static final class Directory implements Entry {
        private final Map<String, Entry> children = new HashMap<>();
    }

    private static final class File implements Entry {
        private final StringBuilder content = new StringBuilder();
    }

    private final Directory root = new Directory();

    public List<String> ls(String path) {
        String[] parts = components(path);
        Entry entry = find(parts);
        if (entry == null) return List.of();
        if (entry instanceof File) return List.of(parts[parts.length - 1]);
        return ((Directory) entry).children.keySet().stream().sorted().toList();
    }

    public void mkdir(String path) {
        String[] parts = components(path);
        directory(parts, parts.length);
    }

    public void addContentToFile(String filePath, String content) {
        String[] parts = components(filePath);
        Objects.requireNonNull(content, "content");
        if (parts.length == 0) throw new IllegalArgumentException("Root is a directory");
        Directory parent = directory(parts, parts.length - 1);
        Entry entry = parent.children.computeIfAbsent(parts[parts.length - 1], ignored -> new File());
        if (!(entry instanceof File file)) {
            throw new IllegalStateException("Path is a directory: " + filePath);
        }
        file.content.append(content);
    }

    public String readContentFromFile(String filePath) {
        Entry entry = find(components(filePath));
        if (entry == null) throw new NoSuchElementException("File not found: " + filePath);
        if (!(entry instanceof File file)) {
            throw new IllegalStateException("Path is a directory: " + filePath);
        }
        return file.content.toString();
    }

    private Entry find(String[] parts) {
        Entry current = root;
        for (String part : parts) {
            current = requireDirectory(current).children.get(part);
            if (current == null) return null;
        }
        return current;
    }

    private Directory directory(String[] parts, int length) {
        Directory current = root;
        for (int i = 0; i < length; i++) {
            Entry child = current.children.computeIfAbsent(parts[i], ignored -> new Directory());
            current = requireDirectory(child);
        }
        return current;
    }

    private static Directory requireDirectory(Entry entry) {
        if (entry instanceof Directory directory) return directory;
        throw new IllegalStateException("Cannot traverse a file as a directory");
    }

    private static String[] components(String path) {
        Objects.requireNonNull(path, "path");
        if (!path.startsWith("/") || path.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Expected an absolute path without NUL characters");
        }
        if (path.equals("/")) return new String[0];
        String[] parts = path.substring(1).split("/", -1);
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                throw new IllegalArgumentException("Expected canonical absolute path: " + path);
            }
        }
        return parts;
    }
}

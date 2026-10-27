package com.conceptandcoding.LowLevelDesign.DesignInMemoryFileSystem;

public final class Main {
    public static void main(String[] args) {
        FileSystem files = new FileSystem();
        System.out.println("Initial root: " + files.ls("/"));
        files.mkdir("/a/b/c");
        files.addContentToFile("/a/b/c/d", "hello");
        System.out.println("Root: " + files.ls("/"));
        System.out.println("Content: " + files.readContentFromFile("/a/b/c/d"));
        files.addContentToFile("/a/b/c/d", " world");
        System.out.println("After append: " + files.readContentFromFile("/a/b/c/d"));
        System.out.println("File listing: " + files.ls("/a/b/c/d"));
        files.mkdir("/a/b/c/z");
        files.addContentToFile("/a/b/c/alpha.txt", "");
        System.out.println("Sorted directory: " + files.ls("/a/b/c"));
    }
}

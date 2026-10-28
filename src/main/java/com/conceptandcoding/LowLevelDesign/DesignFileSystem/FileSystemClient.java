package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

import java.util.List;

public class FileSystemClient {
    public static void main(String[] args) {
        FileSystem fileSystem = new FileSystem();
        FileSystemController controller = new FileSystemController(fileSystem);
        controller.handleCreateDirectory("/", "docs");
        controller.handleCreateFile("/docs", "note.txt");
        controller.handleWriteFile("/docs", "note.txt", "Hello, World!");
        System.out.println("Read file: " + controller.handleReadFile("/docs", "note.txt"));
        List<FileSystemEntry> entries = controller.handleListEntries("/");
        System.out.println("Entries in /: " + entries.size());

        // Expected errors demonstrate the controller's error reporting.
        controller.handleCreateFile("/docs", "note.txt");
        controller.handleReadFile("/", "invalid.txt");
        controller.handleListEntries("/invalid");
        controller.handleDeleteEntry("/", "docs");
        controller.handleReadFile("/docs", "note.txt");
    }
}

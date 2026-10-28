package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Standalone tests requiring only Java 17. */
public final class FileSystemTest {
    public static void main(String[] args) throws Exception {
        FileSystem fs = new FileSystem();
        check(fs.navigateTo("/") == fs.getRoot(), "Root path must resolve");
        check(fs.navigateTo("") == fs.getRoot(), "Empty path must resolve to root");
        check(fs.getRoot().getParent() == null && fs.getRoot().isDirectory(), "Invalid root metadata");
        fs.createDirectory("/", "docs");
        fs.createDirectory("/docs", "nested");
        fs.createFile("/docs", "note.txt");
        Directory docs = fs.navigateTo("/docs");
        FileSystemEntry note = docs.getEntry("note.txt");
        check(note.getName().equals("note.txt") && note.getParent() == docs && !note.isDirectory(),
                "File metadata or parent link incorrect");
        check(fs.navigateTo("/docs/nested").getParent() == docs, "Directory parent link incorrect");
        check(fs.readFile("/docs", "note.txt").isEmpty(), "New file must be empty");
        fs.writeFile("/docs", "note.txt", "first");
        fs.writeFile("/docs", "note.txt", "replacement 世界");
        check(fs.readFile("/docs", "note.txt").equals("replacement 世界"), "Write must replace content");
        List<FileSystemEntry> copy = docs.listEntries();
        copy.clear();
        check(docs.listEntries().size() == 2, "Listing must copy the entries list");
        expectFailure(() -> fs.createFile("/docs", "note.txt"));
        expectFailure(() -> fs.createDirectory("/docs", "note.txt"));
        expectFailure(() -> fs.createFile("/", "docs"));
        expectFailure(() -> fs.createFile("/missing", "file"));
        expectFailure(() -> fs.navigateTo("/docs/note.txt"));
        expectFailure(() -> fs.navigateTo("/docs/note.txt/child"));
        expectFailure(() -> fs.readFile("/docs", "nested"));
        expectFailure(() -> fs.writeFile("/docs", "nested", "bad"));
        expectFailure(() -> fs.readFile("/docs", "missing"));
        expectFailure(() -> fs.writeFile("/docs", "missing", "bad"));
        expectFailure(() -> fs.deleteEntry("/docs", "missing"));
        fs.createFile("/docs/nested", "inside");
        fs.deleteEntry("/docs", "nested");
        expectFailure(() -> fs.navigateTo("/docs/nested"));
        check(docs.getEntry("nested") == null, "Deletion must remove directory entry");
        fs.deleteEntry("/docs", "note.txt");
        check(docs.listEntries().isEmpty(), "Files should be removable");
        fs.createFile("/docs", "note.txt");
        check(fs.readFile("/docs", "note.txt").isEmpty(), "Recreation must not retain old content");
        controllerBehavior();
        System.out.println("Kavriq file system tests passed");
    }

    private static void controllerBehavior() throws Exception {
        FileSystem fs = new FileSystem();
        FileSystemController controller = new FileSystemController(fs);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        PrintStream originalErrors = System.err;
        try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream err = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            System.setErr(err);
            controller.handleCreateDirectory("/", "docs");
            controller.handleCreateFile("/docs", "note");
            controller.handleWriteFile("/docs", "note", "content");
            check(controller.handleReadFile("/docs", "note").equals("content"), "Controller read failed");
            check(controller.handleListEntries("/docs").size() == 1, "Controller listing failed");
            controller.handleCreateFile("/docs", "note");
            check(controller.handleReadFile("/docs", "missing").isEmpty(), "Read error must return empty string");
            check(controller.handleListEntries("/missing").isEmpty(), "Listing error must return empty list");
            controller.handleDeleteEntry("/", "docs");
        } finally {
            System.setOut(originalOutput);
            System.setErr(originalErrors);
        }
        check(output.toString(StandardCharsets.UTF_8).contains("Deleted entry: docs"), "Success not reported");
        String errorText = errors.toString(StandardCharsets.UTF_8);
        check(errorText.contains("Entry already exists") && errorText.contains("File not found")
                && errorText.contains("Invalid path"), "Controller must report expected errors");
        check(fs.getRoot().listEntries().isEmpty(), "Controller delete failed");
    }

    @FunctionalInterface
    private interface Operation { void run() throws FileSystemException; }

    private static void expectFailure(Operation operation) throws FileSystemException {
        try { operation.run(); }
        catch (FileSystemException expected) { return; }
        throw new AssertionError("Expected FileSystemException");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

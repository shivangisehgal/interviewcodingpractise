package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

import java.util.ArrayList;
import java.util.List;

public class FileSystemController {
    private final FileSystem fileSystem;
    public FileSystemController(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    public void handleCreateFile(String path, String name) {
        try {
            fileSystem.createFile(path, name);
            System.out.println("Created file: " + name + " in " + path);
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    public void handleCreateDirectory(String path, String name) {
        try {
            fileSystem.createDirectory(path, name);
            System.out.println("Created directory: " + name + " in " + path);
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    public void handleDeleteEntry(String path, String name) {
        try {
            fileSystem.deleteEntry(path, name);
            System.out.println("Deleted entry: " + name + " from " + path);
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    public void handleWriteFile(String path, String name, String content) {
        try {
            fileSystem.writeFile(path, name, content);
            System.out.println("Wrote content to file: " + name);
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    public String handleReadFile(String path, String name) {
        try {
            return fileSystem.readFile(path, name);
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
            return "";
        }
    }
    public List<FileSystemEntry> handleListEntries(String path) {
        try {
            return fileSystem.navigateTo(path).listEntries();
        }
        catch (FileSystemException e) {
            System.err.println("Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}

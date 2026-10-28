package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

public class FileSystem {
    private Directory root;
    public FileSystem() {
        this.root = new Directory("root", null);
    }
    public Directory getRoot() {
        return root;
    }
    public Directory navigateTo(String path) throws FileSystemException {
        if (path.isEmpty() || path.equals("/")) {
            return root;
        }
        String[] parts = path.split("/");
        Directory current = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            FileSystemEntry entry = current.getEntry(part);
            if (entry == null || !entry.isDirectory()) {
                throw new FileSystemException("Invalid path: " + path);
            }
            current = (Directory) entry;
        }
        return current;
    }
    public void createFile(String path, String name) throws FileSystemException {
        Directory dir = navigateTo(path);
        dir.addEntry(new File(name, dir));
    }
    public void createDirectory(String path, String name) throws FileSystemException {
        Directory dir = navigateTo(path);
        dir.addEntry(new Directory(name, dir));
    }
    public void deleteEntry(String path, String name) throws FileSystemException {
        Directory dir = navigateTo(path);
        dir.removeEntry(name);
    }
    public void writeFile(String path, String name, String content) throws FileSystemException {
        Directory dir = navigateTo(path);
        FileSystemEntry entry = dir.getEntry(name);
        if (entry == null || entry.isDirectory()) {
            throw new FileSystemException("File not found: " + name);
        }
        ((File) entry).setContent(content);
    }
    public String readFile(String path, String name) throws FileSystemException {
        Directory dir = navigateTo(path);
        FileSystemEntry entry = dir.getEntry(name);
        if (entry == null || entry.isDirectory()) {
            throw new FileSystemException("File not found: " + name);
        }
        return ((File) entry).getContent();
    }
}

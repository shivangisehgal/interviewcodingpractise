package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

public abstract class FileSystemEntry {
    protected String name;
    protected Directory parent;
    public FileSystemEntry(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }
    public String getName() {
        return name;
    }
    public Directory getParent() {
        return parent;
    }
    public abstract boolean isDirectory();
}

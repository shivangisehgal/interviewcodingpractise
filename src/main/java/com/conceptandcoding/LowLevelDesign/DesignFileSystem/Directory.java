package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

import java.util.ArrayList;
import java.util.List;

public class Directory extends FileSystemEntry {
    private List<FileSystemEntry> entries;
    public Directory(String name, Directory parent) {
        super(name, parent);
        this.entries = new ArrayList<>();
    }
    @Override    public boolean isDirectory() {
        return true;
    }
    public void addEntry(FileSystemEntry entry) throws FileSystemException {
        for (FileSystemEntry e : entries) {
            if (e.getName().equals(entry.getName())) {
                throw new FileSystemException("Entry already exists: " + entry.getName());
            }
        }
        entries.add(entry);
    }
    public void removeEntry(String name) throws FileSystemException {
        FileSystemEntry entry = getEntry(name);
        if (entry == null) {
            throw new FileSystemException("Entry not found: " + name);
        }
        entries.remove(entry);
    }
    public FileSystemEntry getEntry(String name) {
        for (FileSystemEntry entry : entries) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }
    public List<FileSystemEntry> listEntries() {
        return new ArrayList<>(entries);
    }
}

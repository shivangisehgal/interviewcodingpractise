package com.conceptandcoding.LowLevelDesign.DesignFileSystem;

public class File extends FileSystemEntry {
    private String content;
    public File(String name, Directory parent) {
        super(name, parent);
        this.content = "";
    }
    @Override    public boolean isDirectory() {
        return false;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}

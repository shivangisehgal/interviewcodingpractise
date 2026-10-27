package com.conceptandcoding.LowLevelDesign.DesignInMemoryDatabase;

public final class Main {
    public static void main(String[] args) {
        InMemoryDatabase<String, Integer> database = new InMemoryDatabase<>();
        database.set("x", 10);
        database.begin();
        database.set("x", 20);
        System.out.println("During transaction: " + database.get("x"));
        database.abort();
        System.out.println("After abort: " + database.get("x"));

        database.begin();
        database.set("x", 30);
        database.begin();
        database.set("x", 40);
        database.set("nullable", null);
        database.commit();
        System.out.println("After inner commit: " + database.get("x"));
        database.abort();
        System.out.println("After outer abort: " + database.get("x"));
        System.out.println("Child-created key exists: " + database.containsKey("nullable"));

        database.begin();
        database.set("x", 50);
        database.commit();
        System.out.println("After outer commit: " + database.get("x"));
    }
}

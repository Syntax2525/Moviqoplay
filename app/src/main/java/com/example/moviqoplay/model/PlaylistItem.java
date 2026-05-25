package com.example.moviqoplay.model;

public class PlaylistItem {
    private final String name;
    private final String description;

    public PlaylistItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

package com.example.moviqoplay.model;

public class PlaylistItem {
    public static final String KEY_ALL_SONGS = "__all_songs__";
    public static final String KEY_FAVORITES = "__favorites__";
    public static final String KEY_ALL_VIDEOS = "__all_videos__";

    private final String name;
    private final String description;
    private final String key;

    public PlaylistItem(String name, String description, String key) {
        this.name = name;
        this.description = description;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public boolean isAllVideos() {
        return KEY_ALL_VIDEOS.equals(key);
    }
}

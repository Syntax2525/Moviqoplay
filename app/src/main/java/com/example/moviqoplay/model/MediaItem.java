package com.example.moviqoplay.model;

public class MediaItem {
    private final String title;
    private final String subtitle;
    private final String meta;

    public MediaItem(String title, String subtitle, String meta) {
        this.title = title;
        this.subtitle = subtitle;
        this.meta = meta;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getMeta() {
        return meta;
    }
}

package com.example.moviqoplay.data;

import com.example.moviqoplay.model.MediaItem;
import com.example.moviqoplay.model.PlaylistItem;

import java.util.Arrays;
import java.util.List;

public final class SampleData {
    private SampleData() {
    }

    public static List<MediaItem> mediaItems() {
        return Arrays.asList(
                new MediaItem("Neon Afterglow", "Asha Vale", "3:42"),
                new MediaItem("Glass Skyline", "Nova Drift", "4:08"),
                new MediaItem("Cinematic Pulse", "Movo AI Mix", "2:58"),
                new MediaItem("Midnight Signal", "Kairo Station", "5:12")
        );
    }

    public static List<MediaItem> videoItems() {
        return Arrays.asList(
                new MediaItem("Aurora Sessions", "4K Music Video", "12 min"),
                new MediaItem("Live at Neon Dome", "Concert", "47 min"),
                new MediaItem("Synthwave Roads", "Visualizer", "8 min")
        );
    }

    public static List<PlaylistItem> playlists() {
        return Arrays.asList(
                new PlaylistItem("AMOLED Nights", "42 tracks · Private"),
                new PlaylistItem("Focus Flow", "28 tracks · Downloaded"),
                new PlaylistItem("Cinematic Drive", "33 tracks · Collaborative")
        );
    }

    public static List<String> moods() {
        return Arrays.asList("Focus", "Workout", "Chill", "Drive", "Party");
    }
}

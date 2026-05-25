package com.example.moviqoplay.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.moviqoplay.model.Song;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PlaylistStore {
    private static final String PREFS = "movoqoplay_playlists";
    private static final String FAVORITES = "favorites";
    private static final String PLAYLIST_NAMES = "playlist_names";

    private PlaylistStore() {
    }

    public static void addFavorite(Context context, Song song) {
        if (song == null || TextUtils.isEmpty(song.getPath())) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> paths = new LinkedHashSet<>(preferences.getStringSet(FAVORITES, new LinkedHashSet<>()));
        paths.add(song.getPath());
        preferences.edit().putStringSet(FAVORITES, paths).apply();
    }

    public static void removeFavorite(Context context, Song song) {
        if (song == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> paths = new LinkedHashSet<>(preferences.getStringSet(FAVORITES, new LinkedHashSet<>()));
        paths.remove(song.getPath());
        preferences.edit().putStringSet(FAVORITES, paths).apply();
    }

    public static List<String> favoritePaths(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new ArrayList<>(preferences.getStringSet(FAVORITES, new LinkedHashSet<>()));
    }

    public static void createPlaylist(Context context, String playlistName) {
        if (TextUtils.isEmpty(playlistName)) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> names = new LinkedHashSet<>(preferences.getStringSet(PLAYLIST_NAMES, new LinkedHashSet<>()));
        names.add(playlistName);
        preferences.edit()
                .putStringSet(PLAYLIST_NAMES, names)
                .putStringSet(keyForPlaylist(playlistName), new LinkedHashSet<>())
                .apply();
    }

    public static void addSong(Context context, String playlistName, Song song) {
        if (TextUtils.isEmpty(playlistName) || song == null || TextUtils.isEmpty(song.getPath())) {
            return;
        }
        createPlaylist(context, playlistName);
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> paths = new LinkedHashSet<>(preferences.getStringSet(keyForPlaylist(playlistName), new LinkedHashSet<>()));
        paths.add(song.getPath());
        preferences.edit().putStringSet(keyForPlaylist(playlistName), paths).apply();
    }

    public static void removeSong(Context context, String playlistName, Song song) {
        if (TextUtils.isEmpty(playlistName) || song == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> paths = new LinkedHashSet<>(preferences.getStringSet(keyForPlaylist(playlistName), new LinkedHashSet<>()));
        paths.remove(song.getPath());
        preferences.edit().putStringSet(keyForPlaylist(playlistName), paths).apply();
    }

    public static List<String> playlistNames(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new ArrayList<>(preferences.getStringSet(PLAYLIST_NAMES, new LinkedHashSet<>()));
    }

    public static List<String> playlistSongPaths(Context context, String playlistName) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new ArrayList<>(preferences.getStringSet(keyForPlaylist(playlistName), new LinkedHashSet<>()));
    }

    private static String keyForPlaylist(String playlistName) {
        return "playlist_" + playlistName.trim().toLowerCase().replace(' ', '_');
    }
}

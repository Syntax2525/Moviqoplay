package com.example.moviqoplay.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.example.moviqoplay.R;
import com.example.moviqoplay.model.PlaylistItem;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LocalMediaRepository {
    public interface LoadCallback {
        void onLoaded(List<Song> songs, List<VideoItem> videos);
    }

    private static final LocalMediaRepository INSTANCE = new LocalMediaRepository();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<Song> cachedSongs = Collections.emptyList();
    private List<VideoItem> cachedVideos = Collections.emptyList();

    private LocalMediaRepository() {
    }

    public static LocalMediaRepository get() {
        return INSTANCE;
    }

    public void load(Context context, LoadCallback callback) {
        Context appContext = context.getApplicationContext();
        executor.execute(() -> {
            List<Song> songs = PermissionsHelper.hasAudioPermission(appContext)
                    ? MediaScannerHelper.scanSongs(appContext)
                    : new ArrayList<>();
            List<VideoItem> videos = PermissionsHelper.hasVideoPermission(appContext)
                    ? MediaScannerHelper.scanVideos(appContext, MediaScannerHelper.SORT_RECENT)
                    : new ArrayList<>();
            cachedSongs = songs;
            cachedVideos = videos;
            mainHandler.post(() -> callback.onLoaded(songs, videos));
        });
    }

    public List<Song> filterSongs(String query) {
        if (TextUtils.isEmpty(query)) {
            return new ArrayList<>(cachedSongs);
        }
        String needle = query.trim().toLowerCase(Locale.getDefault());
        List<Song> matches = new ArrayList<>();
        for (Song song : cachedSongs) {
            if (contains(song.getTitle(), needle)
                    || contains(song.getArtist(), needle)
                    || contains(song.getAlbum(), needle)) {
                matches.add(song);
            }
        }
        return matches;
    }

    public List<VideoItem> filterVideos(String query) {
        if (TextUtils.isEmpty(query)) {
            return new ArrayList<>(cachedVideos);
        }
        String needle = query.trim().toLowerCase(Locale.getDefault());
        List<VideoItem> matches = new ArrayList<>();
        for (VideoItem video : cachedVideos) {
            if (contains(video.getTitle(), needle) || contains(video.getFolderName(), needle)) {
                matches.add(video);
            }
        }
        return matches;
    }

    public List<Song> favoriteSongs(Context context) {
        Set<String> favoritePaths = new HashSet<>(PlaylistStore.favoritePaths(context));
        if (favoritePaths.isEmpty()) {
            return Collections.emptyList();
        }
        List<Song> favorites = new ArrayList<>();
        for (Song song : cachedSongs) {
            if (favoritePaths.contains(song.getPath())) {
                favorites.add(song);
            }
        }
        return favorites;
    }

    public List<Song> songsForPlaylist(Context context, String playlistKey) {
        if (PlaylistItem.KEY_ALL_SONGS.equals(playlistKey)) {
            return new ArrayList<>(cachedSongs);
        }
        if (PlaylistItem.KEY_FAVORITES.equals(playlistKey)) {
            return favoriteSongs(context);
        }
        Set<String> paths = new HashSet<>(PlaylistStore.playlistSongPaths(context, playlistKey));
        List<Song> songs = new ArrayList<>();
        for (Song song : cachedSongs) {
            if (paths.contains(song.getPath())) {
                songs.add(song);
            }
        }
        return songs;
    }

    public List<PlaylistItem> buildPlaylistItems(Context context) {
        List<PlaylistItem> items = new ArrayList<>();
        int songCount = cachedSongs.size();
        int videoCount = cachedVideos.size();
        int favoriteCount = favoriteSongs(context).size();

        items.add(new PlaylistItem(
                context.getString(R.string.all_songs),
                context.getString(R.string.playlist_track_count, songCount),
                PlaylistItem.KEY_ALL_SONGS));
        items.add(new PlaylistItem(
                context.getString(R.string.favorites_playlist),
                context.getString(R.string.playlist_track_count, favoriteCount),
                PlaylistItem.KEY_FAVORITES));
        for (String name : PlaylistStore.playlistNames(context)) {
            int count = PlaylistStore.playlistSongPaths(context, name).size();
            items.add(new PlaylistItem(
                    name,
                    context.getString(R.string.playlist_track_count, count),
                    name));
        }
        items.add(new PlaylistItem(
                context.getString(R.string.all_videos),
                context.getString(R.string.playlist_video_count, videoCount),
                PlaylistItem.KEY_ALL_VIDEOS));
        return items;
    }

    public static <T> List<T> preview(List<T> source, int maxItems) {
        if (source == null || source.isEmpty() || source.size() <= maxItems) {
            return source == null ? Collections.emptyList() : source;
        }
        return new ArrayList<>(source.subList(0, maxItems));
    }

    public static int indexOfSong(List<Song> library, Song song) {
        if (library == null || song == null) {
            return -1;
        }
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i).getId() == song.getId()) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfVideo(List<VideoItem> library, VideoItem video) {
        if (library == null || video == null) {
            return -1;
        }
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i).getId() == video.getId()) {
                return i;
            }
        }
        return -1;
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(needle);
    }
}

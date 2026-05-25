package com.example.moviqoplay.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.example.moviqoplay.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MediaUtils {
    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_ALBUM = "Unknown Album";

    private MediaUtils() {
    }

    public static List<Song> scanLocalSongs(Context context) {
        List<Song> songs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = resolver.query(collection, projection, selection, null, sortOrder)) {
            if (cursor == null) {
                return songs;
            }
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String path = cursor.getString(pathColumn);
                if (!isSupportedAudio(path)) {
                    continue;
                }
                long albumId = cursor.getLong(albumIdColumn);
                songs.add(new Song(
                        id,
                        safeText(cursor.getString(titleColumn), titleFromPath(path)),
                        safeText(cursor.getString(artistColumn), UNKNOWN_ARTIST),
                        safeText(cursor.getString(albumColumn), UNKNOWN_ALBUM),
                        cursor.getLong(durationColumn),
                        path,
                        albumArtUri(albumId)
                ));
            }
        } catch (RuntimeException ignored) {
            return songs;
        }
        return songs;
    }

    public static Song readMetadataFromPath(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return new Song(
                    path.hashCode(),
                    safeText(title, titleFromPath(path)),
                    safeText(artist, UNKNOWN_ARTIST),
                    safeText(album, UNKNOWN_ALBUM),
                    parseDuration(duration),
                    path,
                    null
            );
        } catch (RuntimeException ignored) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
                // Best effort cleanup for platform retriever resources.
            }
        }
    }

    public static String formatDuration(long durationMs) {
        long totalSeconds = Math.max(durationMs, 0) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    public static boolean isSupportedAudio(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String lower = path.toLowerCase(Locale.US);
        return lower.endsWith(".mp3")
                || lower.endsWith(".wav")
                || lower.endsWith(".m4a")
                || lower.endsWith(".aac")
                || lower.endsWith(".flac");
    }

    private static String albumArtUri(long albumId) {
        if (albumId <= 0) {
            return null;
        }
        return Uri.parse("content://media/external/audio/albumart").buildUpon()
                .appendPath(String.valueOf(albumId))
                .build()
                .toString();
    }

    private static String titleFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "Unknown Song";
        }
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String safeText(String value, String fallback) {
        return TextUtils.isEmpty(value) || "<unknown>".equalsIgnoreCase(value) ? fallback : value;
    }

    private static long parseDuration(String duration) {
        try {
            return Long.parseLong(duration);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}

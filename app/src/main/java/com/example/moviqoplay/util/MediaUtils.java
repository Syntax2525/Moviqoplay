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
        return MediaScannerHelper.scanSongs(context);
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

    public static boolean isSupportedVideo(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String lower = path.toLowerCase(Locale.US);
        return lower.endsWith(".mp4")
                || lower.endsWith(".mkv")
                || lower.endsWith(".webm")
                || lower.endsWith(".3gp")
                || lower.endsWith(".mov");
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

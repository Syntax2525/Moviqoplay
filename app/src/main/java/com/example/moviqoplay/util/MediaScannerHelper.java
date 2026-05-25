package com.example.moviqoplay.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MediaScannerHelper {
    public static final String SORT_RECENT = "recent";
    public static final String SORT_NAME = "name";
    public static final String SORT_SIZE = "size";
    public static final String SORT_DURATION = "duration";

    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_ALBUM = "Unknown Album";

    private MediaScannerHelper() {
    }

    public static List<Song> scanSongs(Context context) {
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

        try (Cursor cursor = resolver.query(collection, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {
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
                if (!MediaUtils.isSupportedAudio(path)) {
                    continue;
                }
                long albumId = cursor.getLong(albumIdColumn);
                songs.add(new Song(
                        id,
                        safeText(cursor.getString(titleColumn), titleFromPath(path, "Unknown Song")),
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

    public static List<VideoItem> scanVideos(Context context, String sortMode) {
        List<VideoItem> videos = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        };

        try (Cursor cursor = resolver.query(collection, projection, null, null, videoSortOrder(sortMode))) {
            if (cursor == null) {
                return videos;
            }
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String path = cursor.getString(pathColumn);
                if (!MediaUtils.isSupportedVideo(path)) {
                    continue;
                }
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                videos.add(new VideoItem(
                        id,
                        safeText(cursor.getString(titleColumn), titleFromPath(path, "Unknown Video")),
                        cursor.getLong(durationColumn),
                        path,
                        contentUri.toString(),
                        safeText(cursor.getString(folderColumn), folderFromPath(path)),
                        cursor.getLong(sizeColumn)
                ));
            }
        } catch (RuntimeException ignored) {
            return videos;
        }
        return videos;
    }

    public static Uri videoContentUri(long id) {
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
    }

    private static String videoSortOrder(String sortMode) {
        if (SORT_NAME.equals(sortMode)) {
            return MediaStore.Video.Media.TITLE + " COLLATE NOCASE ASC";
        } else if (SORT_SIZE.equals(sortMode)) {
            return MediaStore.Video.Media.SIZE + " DESC";
        } else if (SORT_DURATION.equals(sortMode)) {
            return MediaStore.Video.Media.DURATION + " DESC";
        }
        String dateColumn = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Video.Media.DATE_MODIFIED
                : MediaStore.Video.Media.DATE_ADDED;
        return dateColumn + " DESC";
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

    private static String folderFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "Phone storage";
        }
        File parent = new File(path).getParentFile();
        return parent == null ? "Phone storage" : parent.getName();
    }

    private static String titleFromPath(String path, String fallback) {
        if (TextUtils.isEmpty(path)) {
            return fallback;
        }
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String safeText(String value, String fallback) {
        return TextUtils.isEmpty(value) || "<unknown>".equalsIgnoreCase(value.toLowerCase(Locale.US)) ? fallback : value;
    }
}

package com.example.moviqoplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.MediaScannerHelper;

import java.io.File;

public class VideoPlaybackManager {
    private static final String PREFS = "video_positions";

    private final Context context;
    private final SharedPreferences preferences;
    private ExoPlayer player;

    public VideoPlaybackManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public ExoPlayer createPlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
        }
        return player;
    }

    public void play(VideoItem video, boolean playWhenReady) {
        ExoPlayer exoPlayer = createPlayer();
        exoPlayer.setMediaItem(buildMediaItem(video));
        exoPlayer.prepare();
        exoPlayer.seekTo(readPosition(video.getId()));
        exoPlayer.setPlayWhenReady(playWhenReady);
    }

    public void savePosition(@Nullable VideoItem video) {
        if (video == null || player == null) {
            return;
        }
        long position = Math.max(player.getCurrentPosition(), 0);
        preferences.edit().putLong(positionKey(video.getId()), position).apply();
    }

    public long readPosition(long videoId) {
        return preferences.getLong(positionKey(videoId), 0L);
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private MediaItem buildMediaItem(VideoItem video) {
        Uri uri = MediaScannerHelper.videoContentUri(video.getId());
        MediaItem.Builder builder = new MediaItem.Builder()
                .setUri(uri)
                .setMediaId(String.valueOf(video.getId()))
                .setTag(video)
                .setMediaMetadata(new androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(video.getTitle())
                        .build());

        Uri subtitle = subtitleUri(video.getPath());
        if (subtitle != null) {
            builder.setSubtitleConfigurations(java.util.Collections.singletonList(
                    new MediaItem.SubtitleConfiguration.Builder(subtitle)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage("local")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build()
            ));
        }
        return builder.build();
    }

    @Nullable
    private Uri subtitleUri(String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            return null;
        }
        int dot = videoPath.lastIndexOf('.');
        String base = dot > 0 ? videoPath.substring(0, dot) : videoPath;
        File subtitle = new File(base + ".srt");
        return subtitle.exists() ? Uri.fromFile(subtitle) : null;
    }

    private String positionKey(long videoId) {
        return "position_" + videoId;
    }
}

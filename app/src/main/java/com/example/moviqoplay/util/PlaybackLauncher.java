package com.example.moviqoplay.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.moviqoplay.MusicService;
import com.example.moviqoplay.PlayerActivity;
import com.example.moviqoplay.R;
import com.example.moviqoplay.VideoPlayerActivity;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public final class PlaybackLauncher {
    private PlaybackLauncher() {
    }

    public static void playSongs(Context context, List<Song> queue, int index) {
        if (queue == null || queue.isEmpty()) {
            Toast.makeText(context, R.string.no_local_songs, Toast.LENGTH_SHORT).show();
            return;
        }
        int safeIndex = Math.max(0, Math.min(index, queue.size() - 1));
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putParcelableArrayListExtra(MusicService.EXTRA_SONGS, new ArrayList<>(queue));
        intent.putExtra(MusicService.EXTRA_INDEX, safeIndex);
        ContextCompat.startForegroundService(context, intent);
        context.startActivity(new Intent(context, PlayerActivity.class));
    }

    public static void playVideos(Context context, List<VideoItem> queue, int index) {
        if (queue == null || queue.isEmpty()) {
            Toast.makeText(context, R.string.no_local_videos, Toast.LENGTH_SHORT).show();
            return;
        }
        int safeIndex = Math.max(0, Math.min(index, queue.size() - 1));
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putParcelableArrayListExtra(VideoPlayerActivity.EXTRA_VIDEOS, new ArrayList<>(queue));
        intent.putExtra(VideoPlayerActivity.EXTRA_INDEX, safeIndex);
        context.startActivity(intent);
    }
}

package com.example.moviqoplay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.MediaUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "com.example.moviqoplay.action.PLAY";
    public static final String ACTION_TOGGLE = "com.example.moviqoplay.action.TOGGLE";
    public static final String ACTION_NEXT = "com.example.moviqoplay.action.NEXT";
    public static final String ACTION_PREVIOUS = "com.example.moviqoplay.action.PREVIOUS";
    public static final String ACTION_SEEK = "com.example.moviqoplay.action.SEEK";
    public static final String ACTION_STATE = "com.example.moviqoplay.action.STATE";
    public static final String EXTRA_SONGS = "extra_songs";
    public static final String EXTRA_INDEX = "extra_index";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_SONG_TITLE = "extra_song_title";
    public static final String EXTRA_SONG_ARTIST = "extra_song_artist";
    public static final String EXTRA_DURATION = "extra_duration";
    public static final String EXTRA_PLAYING = "extra_playing";
    public static final String EXTRA_SONG_ID = "extra_song_id";

    private static final String CHANNEL_ID = "movoqoplay_music";
    private static final int NOTIFICATION_ID = 4001;

    private final IBinder binder = new LocalBinder();
    private final List<Song> queue = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private int currentIndex = -1;

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, getString(R.string.app_name));
        mediaSession.setActive(true);
        createNotificationChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            publishState();
            return START_STICKY;
        }
        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            ArrayList<Song> songs = readSongQueue(intent);
            int index = intent.getIntExtra(EXTRA_INDEX, 0);
            playQueue(songs, index);
        } else if (ACTION_TOGGLE.equals(action)) {
            toggle();
        } else if (ACTION_NEXT.equals(action)) {
            next();
        } else if (ACTION_PREVIOUS.equals(action)) {
            previous();
        } else if (ACTION_SEEK.equals(action)) {
            seekTo(intent.getIntExtra(EXTRA_POSITION, 0));
        }
        return START_STICKY;
    }

    public void playQueue(List<Song> songs, int index) {
        if (songs == null || songs.isEmpty()) {
            return;
        }
        queue.clear();
        queue.addAll(songs);
        currentIndex = Math.max(0, Math.min(index, queue.size() - 1));
        playCurrent();
    }

    public void toggle() {
        if (mediaPlayer == null) {
            if (!queue.isEmpty()) {
                playCurrent();
            }
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopForeground(STOP_FOREGROUND_DETACH);
        } else if (requestAudioFocus()) {
            mediaPlayer.start();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        publishState();
        updateNotification();
    }

    public void next() {
        if (queue.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % queue.size();
        playCurrent();
    }

    public void previous() {
        if (queue.isEmpty()) {
            return;
        }
        currentIndex = currentIndex <= 0 ? queue.size() - 1 : currentIndex - 1;
        playCurrent();
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(Math.max(positionMs, 0));
            publishState();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public Song getCurrentSong() {
        if (currentIndex < 0 || currentIndex >= queue.size()) {
            return null;
        }
        return queue.get(currentIndex);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        releasePlayer();
        publishState();
        return true;
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        abandonAudioFocus();
        if (mediaSession != null) {
            mediaSession.release();
        }
        super.onDestroy();
    }

    private void playCurrent() {
        Song song = getCurrentSong();
        if (song == null || TextUtils.isEmpty(song.getPath()) || !MediaUtils.isSupportedAudio(song.getPath())) {
            next();
            return;
        }
        if (!requestAudioFocus()) {
            return;
        }
        releasePlayer();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(this, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId()));
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepare();
            mediaPlayer.start();
            startForeground(NOTIFICATION_ID, buildNotification());
            publishState();
        } catch (Exception ignored) {
            releasePlayer();
            publishState();
        }
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) {
            return true;
        }
        if (focusRequest == null) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setOnAudioFocusChangeListener(this::handleAudioFocusChange)
                    .build();
        }
        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void handleAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS && mediaPlayer != null) {
            mediaPlayer.pause();
            publishState();
            updateNotification();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            publishState();
            updateNotification();
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) {
            return;
        }
        if (focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
    }

    @SuppressWarnings("deprecation")
    private ArrayList<Song> readSongQueue(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableArrayListExtra(EXTRA_SONGS, Song.class);
        }
        return intent.getParcelableArrayListExtra(EXTRA_SONGS);
    }

    private Notification buildNotification() {
        Song song = getCurrentSong();
        Intent contentIntent = new Intent(this, PlayerActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action previous = new NotificationCompat.Action(
                R.drawable.ic_placeholder,
                "Previous",
                servicePendingIntent(ACTION_PREVIOUS, 1)
        );
        NotificationCompat.Action playPause = new NotificationCompat.Action(
                R.drawable.ic_placeholder,
                isPlaying() ? "Pause" : "Play",
                servicePendingIntent(ACTION_TOGGLE, 2)
        );
        NotificationCompat.Action next = new NotificationCompat.Action(
                R.drawable.ic_placeholder,
                "Next",
                servicePendingIntent(ACTION_NEXT, 3)
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_placeholder)
                .setContentTitle(song != null ? song.getTitle() : getString(R.string.app_name))
                .setContentText(song != null ? song.getArtist() : getString(R.string.no_local_songs))
                .setLargeIcon(loadAlbumBitmap(song))
                .setContentIntent(contentPendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying())
                .addAction(previous)
                .addAction(playPause)
                .addAction(next)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private PendingIntent servicePendingIntent(String action, int requestCode) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private Bitmap loadAlbumBitmap(Song song) {
        if (song == null || TextUtils.isEmpty(song.getAlbumArtUri())) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        }
        try (InputStream stream = getContentResolver().openInputStream(Uri.parse(song.getAlbumArtUri()))) {
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ignored) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        }
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private void publishState() {
        Song song = getCurrentSong();
        Intent state = new Intent(ACTION_STATE);
        state.setPackage(getPackageName());
        state.putExtra(EXTRA_PLAYING, isPlaying());
        state.putExtra(EXTRA_POSITION, getCurrentPosition());
        state.putExtra(EXTRA_DURATION, getDuration());
        if (song != null) {
            state.putExtra(EXTRA_SONG_ID, song.getId());
            state.putExtra(EXTRA_SONG_TITLE, song.getTitle());
            state.putExtra(EXTRA_SONG_ARTIST, song.getArtist());
        }
        sendBroadcast(state);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.music_channel_name),
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = ContextCompat.getSystemService(this, NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}

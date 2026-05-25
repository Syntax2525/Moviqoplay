package com.example.moviqoplay;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;

import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.MediaUtils;
import com.example.moviqoplay.util.PlaylistStore;

public class PlayerActivity extends BaseActivity {
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private MusicService musicService;
    private boolean bound;
    private boolean userSeeking;
    private boolean favorite;
    private ImageButton playPause;
    private SeekBar progress;
    private TextView title;
    private TextView artist;
    private TextView currentTime;
    private TextView totalTime;
    private ImageView artwork;
    private ObjectAnimator artworkRotation;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true;
            updateFromService();
            progressHandler.post(progressRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            musicService = null;
        }
    };

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (bound && musicService != null && !userSeeking) {
                int duration = musicService.getDuration();
                int position = musicService.getCurrentPosition();
                progress.setMax(Math.max(duration, 1));
                progress.setProgress(Math.min(position, duration));
                currentTime.setText(MediaUtils.formatDuration(position));
                totalTime.setText(MediaUtils.formatDuration(duration));
                setArtworkAnimation(musicService.isPlaying());
            }
            progressHandler.postDelayed(this, 500);
        }
    };

    private final BroadcastReceiver playbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_STATE.equals(intent.getAction())) {
                title.setText(intent.getStringExtra(MusicService.EXTRA_SONG_TITLE));
                artist.setText(intent.getStringExtra(MusicService.EXTRA_SONG_ARTIST));
                currentTime.setText(MediaUtils.formatDuration(intent.getIntExtra(MusicService.EXTRA_POSITION, 0)));
                totalTime.setText(MediaUtils.formatDuration(intent.getIntExtra(MusicService.EXTRA_DURATION, 0)));
                setArtworkAnimation(intent.getBooleanExtra(MusicService.EXTRA_PLAYING, false));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        ImageButton collapse = findViewById(R.id.btn_collapse);
        playPause = findViewById(R.id.btn_play_pause);
        ImageButton favoriteButton = findViewById(R.id.btn_favorite);
        ImageButton previous = findViewById(R.id.btn_previous);
        ImageButton next = findViewById(R.id.btn_next);
        ImageButton queue = findViewById(R.id.btn_queue);
        ImageButton lyrics = findViewById(R.id.btn_lyrics);
        progress = findViewById(R.id.seek_progress);
        title = findViewById(R.id.txt_track_title);
        artist = findViewById(R.id.txt_track_artist);
        currentTime = findViewById(R.id.txt_time_current);
        totalTime = findViewById(R.id.txt_time_total);
        artwork = findViewById(R.id.img_artwork);

        artwork.setImageResource(R.drawable.sample_album);
        title.setText(R.string.sample_track_title);
        artist.setText(R.string.sample_track_artist);
        setupArtworkAnimation();

        collapse.setOnClickListener(view -> finish());
        playPause.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_TOGGLE));
        previous.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_PREVIOUS));
        next.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_NEXT));
        favoriteButton.setOnClickListener(view -> toggleFavorite());
        queue.setOnClickListener(view -> openScreen(PlaylistActivity.class));
        lyrics.setOnClickListener(view -> openScreen(RecommendationActivity.class));
        findViewById(R.id.btn_more).setOnClickListener(view -> openScreen(EqualizerActivity.class));
        setupSeekBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter(MusicService.ACTION_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playbackReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        progressHandler.removeCallbacks(progressRunnable);
        unregisterReceiver(playbackReceiver);
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        super.onStop();
    }

    private void setupSeekBar() {
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    currentTime.setText(MediaUtils.formatDuration(value));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
                Intent intent = new Intent(PlayerActivity.this, MusicService.class);
                intent.setAction(MusicService.ACTION_SEEK);
                intent.putExtra(MusicService.EXTRA_POSITION, seekBar.getProgress());
                startService(intent);
            }
        });
    }

    private void setupArtworkAnimation() {
        artworkRotation = ObjectAnimator.ofFloat(artwork, "rotation", 0f, 360f);
        artworkRotation.setDuration(18000);
        artworkRotation.setRepeatCount(ObjectAnimator.INFINITE);
        artworkRotation.setInterpolator(new LinearInterpolator());
    }

    private void setArtworkAnimation(boolean playing) {
        if (playing && !artworkRotation.isStarted()) {
            artworkRotation.start();
        } else if (!playing && artworkRotation.isStarted()) {
            artworkRotation.pause();
        }
    }

    private void updateFromService() {
        if (musicService == null) {
            return;
        }
        Song song = musicService.getCurrentSong();
        if (song != null) {
            title.setText(song.getTitle());
            artist.setText(song.getArtist());
        }
        setArtworkAnimation(musicService.isPlaying());
    }

    private void sendPlaybackAction(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent);
    }

    private void toggleFavorite() {
        favorite = !favorite;
        if (musicService != null && musicService.getCurrentSong() != null) {
            if (favorite) {
                PlaylistStore.addFavorite(this, musicService.getCurrentSong());
            } else {
                PlaylistStore.removeFavorite(this, musicService.getCurrentSong());
            }
        }
    }
}

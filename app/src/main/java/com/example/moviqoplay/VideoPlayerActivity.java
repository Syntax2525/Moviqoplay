package com.example.moviqoplay;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.VideoAdapter;
import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.MediaScannerHelper;
import com.example.moviqoplay.util.MediaUtils;
import com.example.moviqoplay.util.PermissionsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoPlayerActivity extends BaseActivity {
    public static final String EXTRA_VIDEOS = "extra_videos";
    public static final String EXTRA_INDEX = "extra_index";

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<VideoItem> videos = new ArrayList<>();
    private VideoPlaybackManager playbackManager;
    private ExoPlayer player;
    private PlayerView playerView;
    private VideoAdapter recommendedAdapter;
    private VideoAdapter localAdapter;
    private ExecutorService scanExecutor;
    private Slider seek;
    private TextView title;
    private TextView meta;
    private TextView currentTime;
    private TextView totalTime;
    private ImageButton playPause;
    private View topOverlay;
    private View centerOverlay;
    private View bottomOverlay;
    private AudioManager audioManager;
    private GestureDetector gestureDetector;
    private LinearLayout videoListSheet;
    private boolean videoListSheetVisible = false;
    private boolean userSeeking;
    private boolean controlsVisible = false;
    private static final long AUTO_HIDE_DELAY_MS = 3000;
    private int currentIndex;
    private String sortMode = MediaScannerHelper.SORT_RECENT;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);

    private final Runnable autoHideRunnable = () -> setControlsVisible(false);

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            progressHandler.postDelayed(this, 500);
        }
    };

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                playNext();
            }
            updateProgress();
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            updatePlayButton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playbackManager = new VideoPlaybackManager(this);
        player = playbackManager.createPlayer();
        audioManager = ContextCompat.getSystemService(this, AudioManager.class);
        scanExecutor = Executors.newSingleThreadExecutor();

        bindViews();
        setupPlayer();
        setupControls();
        readIntentQueue();

        if (PermissionsHelper.hasVideoPermission(this)) {
            scanVideos();
        } else {
            PermissionsHelper.requestMediaPermissions(this, permissionLauncher);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        player.addListener(playerListener);
        progressHandler.post(progressRunnable);
    }

    @Override
    protected void onStop() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.removeCallbacks(autoHideRunnable);
        player.removeListener(playerListener);
        playbackManager.savePosition(currentVideo());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (scanExecutor != null) {
            scanExecutor.shutdownNow();
        }
        playbackManager.release();
        super.onDestroy();
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPipIfPossible();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            setControlsVisible(false);
            if (videoListSheetVisible) {
                toggleVideoListSheet();
            }
        }
    }

    private void bindViews() {
        playerView = findViewById(R.id.player_view);
        seek = findViewById(R.id.seek_video);
        title = findViewById(R.id.txt_video_title);
        meta = findViewById(R.id.txt_video_meta);
        currentTime = findViewById(R.id.txt_video_time);
        totalTime = findViewById(R.id.txt_video_duration);
        playPause = findViewById(R.id.btn_video_play);
        topOverlay = findViewById(R.id.overlay_top);
        centerOverlay = findViewById(R.id.overlay_center);
        bottomOverlay = findViewById(R.id.overlay_bottom);
        videoListSheet = findViewById(R.id.video_list_sheet);

        RecyclerView recommended = findViewById(R.id.rv_recommended_videos);
        RecyclerView local = findViewById(R.id.rv_local_videos);
        setupHorizontalList(recommended);
        local.setLayoutManager(new GridLayoutManager(this, 2));
        local.setHasFixedSize(true);
        recommendedAdapter = new VideoAdapter(this::playVideoFromList);
        localAdapter = new VideoAdapter(this::playVideoFromList);
        recommended.setAdapter(recommendedAdapter);
        local.setAdapter(localAdapter);
    }

    private void setupPlayer() {
        playerView.setPlayer(player);
        hideSystemBars();
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (videoListSheetVisible) {
                    toggleVideoListSheet();
                    return true;
                }
                setControlsVisible(!controlsVisible);
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (e.getX() < playerView.getWidth() / 2f) {
                    seekBy(-10000);
                } else {
                    seekBy(10000);
                }
                return true;
            }
        });
        playerView.setOnTouchListener(this::handleTouch);
    }

    private void setupControls() {
        findViewById(R.id.btn_video_back).setOnClickListener(view -> finish());
        findViewById(R.id.btn_video_previous).setOnClickListener(view -> playPrevious());
        findViewById(R.id.btn_video_next).setOnClickListener(view -> playNext());
        findViewById(R.id.btn_video_rewind).setOnClickListener(view -> seekBy(-10000));
        findViewById(R.id.btn_video_forward).setOnClickListener(view -> seekBy(10000));
        findViewById(R.id.btn_video_fullscreen).setOnClickListener(view -> toggleOrientation());
        findViewById(R.id.btn_video_pip).setOnClickListener(view -> enterPipIfPossible());
        findViewById(R.id.btn_video_speed).setOnClickListener(this::showSpeedMenu);
        findViewById(R.id.btn_subtitles).setOnClickListener(view ->
                Toast.makeText(this, "Local .srt subtitles with matching file names load automatically.", Toast.LENGTH_SHORT).show());
        MaterialButton sortButton = findViewById(R.id.btn_sort_videos);
        sortButton.setOnClickListener(view -> showSortMenu(sortButton));
        playPause.setOnClickListener(view -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
            updatePlayButton();
        });
        seek.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                currentTime.setText(MediaUtils.formatDuration((long) value));
            }
        });
        seek.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                userSeeking = true;
                progressHandler.removeCallbacks(autoHideRunnable);
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                userSeeking = false;
                player.seekTo((long) slider.getValue());
                scheduleAutoHide();
            }
        });
        findViewById(R.id.btn_video_list).setOnClickListener(v -> toggleVideoListSheet());
    }

    @SuppressWarnings("deprecation")
    private void readIntentQueue() {
        Intent intent = getIntent();
        ArrayList<VideoItem> intentVideos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentVideos = intent.getParcelableArrayListExtra(EXTRA_VIDEOS, VideoItem.class);
        } else {
            intentVideos = intent.getParcelableArrayListExtra(EXTRA_VIDEOS);
        }
        if (intentVideos != null && !intentVideos.isEmpty()) {
            videos.clear();
            videos.addAll(intentVideos);
            currentIndex = Math.max(0, Math.min(intent.getIntExtra(EXTRA_INDEX, 0), videos.size() - 1));
            updateAdapters();
            playCurrent(true);
        }
    }

    private void scanVideos() {
        scanExecutor.execute(() -> {
            List<VideoItem> scanned = MediaScannerHelper.scanVideos(this, sortMode);
            runOnUiThread(() -> {
                if (!scanned.isEmpty()) {
                    videos.clear();
                    videos.addAll(scanned);
                    currentIndex = Math.min(currentIndex, videos.size() - 1);
                    updateAdapters();
                    if (player.getMediaItemCount() == 0) {
                        playCurrent(false);
                    }
                } else {
                    title.setText(R.string.no_local_videos);
                    meta.setText(R.string.local_video_library);
                    updateAdapters();
                }
            });
        });
    }

    private void playVideoFromList(VideoItem video, int position) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        playbackManager.savePosition(currentVideo());
        currentIndex = Math.max(0, Math.min(position, videos.size() - 1));
        playCurrent(true);
    }

    private void playCurrent(boolean playWhenReady) {
        VideoItem video = currentVideo();
        if (video == null) {
            return;
        }
        title.setText(video.getTitle());
        meta.setText(video.getFolderName());
        playbackManager.play(video, playWhenReady);
        updateProgress();
        updatePlayButton();
    }

    private void playNext() {
        if (videos.isEmpty()) {
            return;
        }
        playbackManager.savePosition(currentVideo());
        currentIndex = (currentIndex + 1) % videos.size();
        playCurrent(true);
    }

    private void playPrevious() {
        if (videos.isEmpty()) {
            return;
        }
        playbackManager.savePosition(currentVideo());
        currentIndex = currentIndex <= 0 ? videos.size() - 1 : currentIndex - 1;
        playCurrent(true);
    }

    private void seekBy(long deltaMs) {
        long duration = Math.max(player.getDuration(), 0);
        long target = Math.max(0, Math.min(player.getCurrentPosition() + deltaMs, duration));
        player.seekTo(target);
        updateProgress();
    }

    private VideoItem currentVideo() {
        if (currentIndex < 0 || currentIndex >= videos.size()) {
            return null;
        }
        return videos.get(currentIndex);
    }

    private void updateProgress() {
        long duration = Math.max(player.getDuration(), 0);
        long position = Math.max(player.getCurrentPosition(), 0);
        seek.setValueTo(Math.max(duration, 1));
        if (!userSeeking) {
            seek.setValue(Math.min(position, seek.getValueTo()));
        }
        currentTime.setText(MediaUtils.formatDuration(position));
        totalTime.setText(MediaUtils.formatDuration(duration));
    }

    private void updatePlayButton() {
        playPause.setSelected(player.isPlaying());
    }

    private void updateAdapters() {
        recommendedAdapter.submitVideos(videos);
        localAdapter.submitVideos(videos);
    }

    private boolean handleTouch(View view, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float height = Math.max(view.getHeight(), 1);
            float percent = 1f - (event.getY() / height);
            if (event.getX() < view.getWidth() / 2f) {
                setBrightness(percent);
            } else {
                setVolume(percent);
            }
        }
        return true;
    }

    private void setBrightness(float value) {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = Math.max(0.05f, Math.min(value, 1f));
        window.setAttributes(params);
    }

    private void setVolume(float value) {
        if (audioManager == null) {
            return;
        }
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = Math.max(0, Math.min(Math.round(max * value), max));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void showSpeedMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        float[] speeds = {0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f};
        for (float speed : speeds) {
            menu.getMenu().add(speed + "x");
        }
        menu.setOnMenuItemClickListener(item -> {
            String label = String.valueOf(item.getTitle()).replace("x", "");
            player.setPlaybackSpeed(Float.parseFloat(label));
            return true;
        });
        menu.show();
    }

    private void showSortMenu(MaterialButton sortButton) {
        PopupMenu menu = new PopupMenu(this, sortButton);
        menu.getMenu().add(R.string.sort_recent);
        menu.getMenu().add(R.string.sort_name);
        menu.getMenu().add(R.string.sort_size);
        menu.getMenu().add(R.string.sort_duration);
        menu.setOnMenuItemClickListener(item -> {
            String label = String.valueOf(item.getTitle());
            if (label.equals(getString(R.string.sort_name))) {
                sortMode = MediaScannerHelper.SORT_NAME;
            } else if (label.equals(getString(R.string.sort_size))) {
                sortMode = MediaScannerHelper.SORT_SIZE;
            } else if (label.equals(getString(R.string.sort_duration))) {
                sortMode = MediaScannerHelper.SORT_DURATION;
            } else {
                sortMode = MediaScannerHelper.SORT_RECENT;
            }
            sortButton.setText(label);
            scanVideos();
            return true;
        });
        menu.show();
    }

    private void toggleOrientation() {
        int orientation = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(orientation);
    }

    private void enterPipIfPossible() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || currentVideo() == null) {
            return;
        }
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(new Rational(16, 9))
                .build();
        enterPictureInPictureMode(params);
    }

    private void setControlsVisible(boolean visible) {
        controlsVisible = visible;
        progressHandler.removeCallbacks(autoHideRunnable);

        float targetAlpha = visible ? 1f : 0f;
        if (visible) {
            topOverlay.setVisibility(View.VISIBLE);
            centerOverlay.setVisibility(View.VISIBLE);
            bottomOverlay.setVisibility(View.VISIBLE);
        }
        topOverlay.animate().alpha(targetAlpha).setDuration(200).setListener(visibilityListener(topOverlay, visible)).start();
        centerOverlay.animate().alpha(targetAlpha).setDuration(200).setListener(visibilityListener(centerOverlay, visible)).start();
        bottomOverlay.animate().alpha(targetAlpha).setDuration(200).setListener(visibilityListener(bottomOverlay, visible)).start();

        if (visible) {
            scheduleAutoHide();
        }
    }

    private AnimatorListenerAdapter visibilityListener(View view, boolean visible) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!visible) {
                    view.setVisibility(View.GONE);
                }
            }
        };
    }

    private void scheduleAutoHide() {
        progressHandler.removeCallbacks(autoHideRunnable);
        progressHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);
    }

    private void toggleVideoListSheet() {
        if (videoListSheetVisible) {
            videoListSheet.animate().translationY(videoListSheet.getHeight()).setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            videoListSheet.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            videoListSheet.setVisibility(View.VISIBLE);
            videoListSheet.setTranslationY(videoListSheet.getHeight() > 0 ? videoListSheet.getHeight() : 1000);
            videoListSheet.animate().translationY(0).setDuration(300).setListener(null).start();
        }
        videoListSheetVisible = !videoListSheetVisible;
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void onPermissionResult(Map<String, Boolean> grants) {
        boolean granted = true;
        for (Boolean value : grants.values()) {
            granted = granted && Boolean.TRUE.equals(value);
        }
        if (granted || PermissionsHelper.hasVideoPermission(this)) {
            scanVideos();
        } else {
            Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
        }
    }
}

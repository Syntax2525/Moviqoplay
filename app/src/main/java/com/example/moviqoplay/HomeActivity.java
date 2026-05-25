package com.example.moviqoplay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moviqoplay.adapter.SongAdapter;
import com.example.moviqoplay.adapter.VideoAdapter;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.MediaScannerHelper;
import com.example.moviqoplay.util.PermissionsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends BaseActivity {
    private final ArrayList<Song> localSongs = new ArrayList<>();
    private final ArrayList<VideoItem> localVideos = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService scanExecutor;
    private SongAdapter recentlyAddedAdapter;
    private SongAdapter localLibraryAdapter;
    private VideoAdapter recentlyWatchedAdapter;
    private VideoAdapter localVideoAdapter;
    private TextView miniTitle;
    private TextView miniArtist;

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (mediaPermissionGranted(grants)) {
                    scanLocalMedia();
                } else {
                    Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewPager2 featuredPager = findViewById(R.id.vp_featured);
        RecyclerView recent = findViewById(R.id.rv_recently_played);
        RecyclerView localLibrary = findViewById(R.id.rv_trending);
        RecyclerView recentlyWatched = findViewById(R.id.rv_recommended);
        RecyclerView localVideoLibrary = findViewById(R.id.rv_local_videos);
        MaterialCardView miniPlayer = findViewById(R.id.mini_player);
        ImageButton play = findViewById(R.id.mini_btn_play);
        ImageButton next = findViewById(R.id.mini_btn_next);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_nav);
        miniTitle = findViewById(R.id.mini_title);
        miniArtist = findViewById(R.id.mini_artist);

        setupHorizontalList(recent);
        setupHorizontalList(localLibrary);
        setupHorizontalList(recentlyWatched);
        localVideoLibrary.setLayoutManager(new GridLayoutManager(this, 2));
        localVideoLibrary.setHasFixedSize(true);
        featuredPager.setOffscreenPageLimit(1);

        recentlyAddedAdapter = new SongAdapter(this::playSong);
        localLibraryAdapter = new SongAdapter(this::playSong);
        recentlyWatchedAdapter = new VideoAdapter(this::playVideo);
        localVideoAdapter = new VideoAdapter(this::playVideo);
        recent.setAdapter(recentlyAddedAdapter);
        localLibrary.setAdapter(localLibraryAdapter);
        recentlyWatched.setAdapter(recentlyWatchedAdapter);
        localVideoLibrary.setAdapter(localVideoAdapter);

        miniPlayer.setOnClickListener(view -> openScreen(PlayerActivity.class));
        play.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_TOGGLE));
        next.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_NEXT));
        findViewById(R.id.btn_notifications).setOnClickListener(view -> openScreen(SettingsActivity.class));

        setupBottomNavigation(bottomNavigation);
        scanExecutor = Executors.newSingleThreadExecutor();
        if (PermissionsHelper.hasMediaPermissions(this)) {
            scanLocalMedia();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    @Override
    protected void onDestroy() {
        if (scanExecutor != null) {
            scanExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    private void scanLocalMedia() {
        scanExecutor.execute(() -> {
            List<Song> songs = PermissionsHelper.hasAudioPermission(this)
                    ? MediaScannerHelper.scanSongs(this)
                    : new ArrayList<>();
            List<VideoItem> videos = PermissionsHelper.hasVideoPermission(this)
                    ? MediaScannerHelper.scanVideos(this, MediaScannerHelper.SORT_RECENT)
                    : new ArrayList<>();
            mainHandler.post(() -> {
                localSongs.clear();
                localSongs.addAll(songs);
                localVideos.clear();
                localVideos.addAll(videos);
                recentlyAddedAdapter.submitSongs(songs);
                localLibraryAdapter.submitSongs(songs);
                recentlyWatchedAdapter.submitVideos(videos);
                localVideoAdapter.submitVideos(videos);
                if (songs.isEmpty()) {
                    miniTitle.setText(R.string.no_local_songs);
                    miniArtist.setText(R.string.app_name);
                } else {
                    Song first = songs.get(0);
                    miniTitle.setText(first.getTitle());
                    miniArtist.setText(first.getArtist());
                }
            });
        });
    }

    private void playSong(Song song, int position) {
        if (localSongs.isEmpty()) {
            return;
        }
        recentlyAddedAdapter.setPlayingSongId(song.getId());
        localLibraryAdapter.setPlayingSongId(song.getId());
        miniTitle.setText(song.getTitle());
        miniArtist.setText(song.getArtist());

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putParcelableArrayListExtra(MusicService.EXTRA_SONGS, localSongs);
        intent.putExtra(MusicService.EXTRA_INDEX, position);
        ContextCompat.startForegroundService(this, intent);
        openScreen(PlayerActivity.class);
    }

    private void playVideo(VideoItem video, int position) {
        if (localVideos.isEmpty()) {
            return;
        }
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putParcelableArrayListExtra(VideoPlayerActivity.EXTRA_VIDEOS, localVideos);
        intent.putExtra(VideoPlayerActivity.EXTRA_INDEX, position);
        startActivity(intent);
    }

    private void sendPlaybackAction(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent);
    }

    private boolean mediaPermissionGranted(Map<String, Boolean> grants) {
        if (PermissionsHelper.hasMediaPermissions(this)) {
            return true;
        }
        boolean granted = false;
        for (Boolean value : grants.values()) {
            granted = granted || Boolean.TRUE.equals(value);
        }
        return granted;
    }

    private void setupBottomNavigation(BottomNavigationView bottomNavigation) {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_search) {
                openScreen(SearchActivity.class);
            } else if (itemId == R.id.nav_library) {
                openScreen(PlaylistActivity.class);
            } else if (itemId == R.id.nav_profile) {
                openScreen(ProfileActivity.class);
            }
            return true;
        });
    }
}

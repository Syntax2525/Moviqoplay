package com.example.moviqoplay;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.SongAdapter;
import com.example.moviqoplay.adapter.VideoAdapter;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.LocalMediaRepository;
import com.example.moviqoplay.util.PlaybackLauncher;
import com.example.moviqoplay.util.PermissionsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends BaseActivity {
    private static final int HOME_PREVIEW_LIMIT = 30;

    private final ArrayList<Song> localSongs = new ArrayList<>();
    private final ArrayList<VideoItem> localVideos = new ArrayList<>();
    private SongAdapter recentlyAddedAdapter;
    private SongAdapter localLibraryAdapter;
    private VideoAdapter recentlyWatchedAdapter;
    private VideoAdapter localVideoAdapter;
    private NestedScrollView scrollHome;
    private MaterialCardView miniPlayer;
    private TextView miniTitle;
    private TextView miniArtist;
    private TextView heroTitle;
    private TextView heroSubtitle;
    private MusicService musicService;
    private boolean serviceBound;

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (mediaPermissionGranted(grants)) {
                    loadLocalMedia();
                } else {
                    Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
                }
            });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            refreshMiniPlayerFromService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            serviceBound = false;
            setMiniPlayerVisible(false);
        }
    };

    private final BroadcastReceiver playbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!MusicService.ACTION_STATE.equals(intent.getAction())) {
                return;
            }
            boolean hasSession = intent.getBooleanExtra(MusicService.EXTRA_HAS_SESSION, false)
                    || (musicService != null && musicService.hasActiveSession());
            if (!hasSession) {
                setMiniPlayerVisible(false);
                return;
            }
            String title = intent.getStringExtra(MusicService.EXTRA_SONG_TITLE);
            String artist = intent.getStringExtra(MusicService.EXTRA_SONG_ARTIST);
            if (title != null) {
                miniTitle.setText(title);
            }
            if (artist != null) {
                miniArtist.setText(artist);
            }
            long songId = intent.getLongExtra(MusicService.EXTRA_SONG_ID, -1L);
            if (songId >= 0) {
                recentlyAddedAdapter.setPlayingSongId(songId);
                localLibraryAdapter.setPlayingSongId(songId);
            }
            setMiniPlayerVisible(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        applyHomeBottomBarInsets(R.id.home_bottom_bar, R.id.scroll_home);

        scrollHome = findViewById(R.id.scroll_home);
        RecyclerView recent = findViewById(R.id.rv_recently_played);
        RecyclerView localLibrary = findViewById(R.id.rv_trending);
        RecyclerView recentlyWatched = findViewById(R.id.rv_recommended);
        RecyclerView localVideoLibrary = findViewById(R.id.rv_local_videos);
        miniPlayer = findViewById(R.id.mini_player);
        ImageButton play = findViewById(R.id.mini_btn_play);
        ImageButton next = findViewById(R.id.mini_btn_next);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_nav);
        miniTitle = findViewById(R.id.mini_title);
        miniArtist = findViewById(R.id.mini_artist);
        heroTitle = findViewById(R.id.txt_hero_title);
        heroSubtitle = findViewById(R.id.txt_hero_subtitle);

        setMiniPlayerVisible(false);
        miniTitle.setText(R.string.nothing_playing);
        miniArtist.setText(R.string.loading_local_media);

        setupHorizontalList(recent);
        setupHorizontalList(localLibrary);
        setupHorizontalList(recentlyWatched);
        localVideoLibrary.setLayoutManager(new GridLayoutManager(this, 2));
        localVideoLibrary.setHasFixedSize(true);

        recentlyAddedAdapter = new SongAdapter(this::playSong, true);
        localLibraryAdapter = new SongAdapter(this::playSong, true);
        recentlyWatchedAdapter = new VideoAdapter(this::playVideo, true);
        localVideoAdapter = new VideoAdapter(this::playVideo, false);
        recent.setAdapter(recentlyAddedAdapter);
        localLibrary.setAdapter(localLibraryAdapter);
        recentlyWatched.setAdapter(recentlyWatchedAdapter);
        localVideoLibrary.setAdapter(localVideoAdapter);

        miniPlayer.setOnClickListener(view -> openScreen(PlayerActivity.class));
        play.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_TOGGLE));
        next.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_NEXT));
        findViewById(R.id.btn_notifications).setOnClickListener(view -> openScreen(SettingsActivity.class));

        findViewById(R.id.btn_hero_play).setOnClickListener(view -> {
            if (!localSongs.isEmpty()) {
                playSong(localSongs.get(0), 0);
            } else {
                Toast.makeText(this, R.string.no_local_songs, Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation(bottomNavigation);
        if (PermissionsHelper.hasMediaPermissions(this)) {
            loadLocalMedia();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter(MusicService.ACTION_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playbackReceiver, filter);
        }
        refreshMiniPlayerFromService();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(playbackReceiver);
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            musicService = null;
        }
        super.onStop();
    }

    private void refreshMiniPlayerFromService() {
        if (musicService == null || !musicService.hasActiveSession()) {
            setMiniPlayerVisible(false);
            return;
        }
        Song song = musicService.getCurrentSong();
        if (song != null) {
            miniTitle.setText(song.getTitle());
            miniArtist.setText(song.getArtist());
            recentlyAddedAdapter.setPlayingSongId(song.getId());
            localLibraryAdapter.setPlayingSongId(song.getId());
        }
        setMiniPlayerVisible(true);
    }

    private void setMiniPlayerVisible(boolean visible) {
        int targetVisibility = visible ? View.VISIBLE : View.GONE;
        if (miniPlayer.getVisibility() == targetVisibility) {
            updateScrollBottomPadding(visible);
            return;
        }
        miniPlayer.setVisibility(targetVisibility);
        updateScrollBottomPadding(visible);
    }

    private void updateScrollBottomPadding(boolean miniVisible) {
        int base = getResources().getDimensionPixelSize(
                miniVisible ? R.dimen.home_scroll_bottom_with_mini : R.dimen.home_scroll_bottom_nav_only);
        scrollHome.setPadding(
                scrollHome.getPaddingLeft(),
                scrollHome.getPaddingTop(),
                scrollHome.getPaddingRight(),
                base);
    }

    private void loadLocalMedia() {
        LocalMediaRepository.get().load(this, (songs, videos) -> {
            localSongs.clear();
            localSongs.addAll(songs);
            localVideos.clear();
            localVideos.addAll(videos);

            List<Song> songPreview = LocalMediaRepository.preview(songs, HOME_PREVIEW_LIMIT);
            List<VideoItem> videoPreview = LocalMediaRepository.preview(videos, HOME_PREVIEW_LIMIT);
            recentlyAddedAdapter.submitSongs(songPreview);
            localLibraryAdapter.submitSongs(songPreview);
            recentlyWatchedAdapter.submitVideos(videoPreview);
            localVideoAdapter.submitVideos(LocalMediaRepository.preview(videos, HOME_PREVIEW_LIMIT * 2));

            updateHeroBanner();
        });
    }

    private void updateHeroBanner() {
        if (localSongs.isEmpty()) {
            heroTitle.setText(R.string.no_local_songs);
            heroSubtitle.setText(R.string.permission_denied_media);
            return;
        }
        Song featured = localSongs.get(0);
        heroTitle.setText(featured.getTitle());
        heroSubtitle.setText(getString(R.string.hero_from_library) + " · " + featured.getArtist());
    }

    private void playSong(Song song, int ignoredAdapterPosition) {
        int queueIndex = LocalMediaRepository.indexOfSong(localSongs, song);
        if (queueIndex < 0) {
            return;
        }
        recentlyAddedAdapter.setPlayingSongId(song.getId());
        localLibraryAdapter.setPlayingSongId(song.getId());
        miniTitle.setText(song.getTitle());
        miniArtist.setText(song.getArtist());
        setMiniPlayerVisible(true);
        PlaybackLauncher.playSongs(this, localSongs, queueIndex);
    }

    private void playVideo(VideoItem video, int ignoredAdapterPosition) {
        int queueIndex = LocalMediaRepository.indexOfVideo(localVideos, video);
        if (queueIndex < 0) {
            return;
        }
        PlaybackLauncher.playVideos(this, localVideos, queueIndex);
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




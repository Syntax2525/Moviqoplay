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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moviqoplay.adapter.MediaAdapter;
import com.example.moviqoplay.adapter.SongAdapter;
import com.example.moviqoplay.data.SampleData;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.MediaUtils;
import com.example.moviqoplay.util.PermissionsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends BaseActivity {
    private final ArrayList<Song> localSongs = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService scanExecutor;
    private SongAdapter recentlyAddedAdapter;
    private SongAdapter localLibraryAdapter;
    private TextView miniTitle;
    private TextView miniArtist;

    private final ActivityResultLauncher<String> audioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    scanLocalMusic();
                } else {
                    Toast.makeText(this, R.string.permission_denied_audio, Toast.LENGTH_LONG).show();
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
        RecyclerView recommended = findViewById(R.id.rv_recommended);
        MaterialCardView miniPlayer = findViewById(R.id.mini_player);
        ImageButton play = findViewById(R.id.mini_btn_play);
        ImageButton next = findViewById(R.id.mini_btn_next);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_nav);
        miniTitle = findViewById(R.id.mini_title);
        miniArtist = findViewById(R.id.mini_artist);

        setupHorizontalList(recent);
        setupHorizontalList(localLibrary);
        setupHorizontalList(recommended);
        featuredPager.setOffscreenPageLimit(1);

        recentlyAddedAdapter = new SongAdapter(this::playSong);
        localLibraryAdapter = new SongAdapter(this::playSong);
        recent.setAdapter(recentlyAddedAdapter);
        localLibrary.setAdapter(localLibraryAdapter);
        recommended.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(RecommendationActivity.class)));

        miniPlayer.setOnClickListener(view -> openScreen(PlayerActivity.class));
        play.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_TOGGLE));
        next.setOnClickListener(view -> sendPlaybackAction(MusicService.ACTION_NEXT));
        findViewById(R.id.btn_notifications).setOnClickListener(view -> openScreen(SettingsActivity.class));

        setupBottomNavigation(bottomNavigation);
        scanExecutor = Executors.newSingleThreadExecutor();
        if (PermissionsHelper.hasAudioPermission(this)) {
            scanLocalMusic();
        } else {
            PermissionsHelper.requestAudioPermission(this, audioPermissionLauncher);
        }
    }

    @Override
    protected void onDestroy() {
        if (scanExecutor != null) {
            scanExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    private void scanLocalMusic() {
        scanExecutor.execute(() -> {
            List<Song> songs = MediaUtils.scanLocalSongs(this);
            mainHandler.post(() -> {
                localSongs.clear();
                localSongs.addAll(songs);
                recentlyAddedAdapter.submitSongs(songs);
                localLibraryAdapter.submitSongs(songs);
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

    private void sendPlaybackAction(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent);
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

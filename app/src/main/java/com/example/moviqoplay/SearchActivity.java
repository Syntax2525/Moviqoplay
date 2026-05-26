package com.example.moviqoplay;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.SongAdapter;
import com.example.moviqoplay.adapter.VideoAdapter;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.model.VideoItem;
import com.example.moviqoplay.util.LocalMediaRepository;
import com.example.moviqoplay.util.PlaybackLauncher;
import com.example.moviqoplay.util.PermissionsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BaseActivity {
    private final ArrayList<Song> allSongs = new ArrayList<>();
    private final ArrayList<VideoItem> allVideos = new ArrayList<>();
    private SongAdapter songAdapter;
    private VideoAdapter videoAdapter;

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (mediaPermissionGranted(grants)) {
                    loadLocalMedia();
                } else {
                    Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        applyTopInset(R.id.card_search);
        applyBottomScrollPadding(R.id.search_scroll, 0);

        EditText search = findViewById(R.id.edit_search);
        ImageButton voice = findViewById(R.id.btn_voice_search);
        RecyclerView musicResults = findViewById(R.id.rv_trending_searches);
        RecyclerView videoResults = findViewById(R.id.rv_recent_searches);

        setupHorizontalList(musicResults);
        setupHorizontalList(videoResults);
        songAdapter = new SongAdapter(this::playSong, true);
        videoAdapter = new VideoAdapter(this::playVideo, true);
        musicResults.setAdapter(songAdapter);
        videoResults.setAdapter(videoAdapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        voice.setOnClickListener(view -> openScreen(RecommendationActivity.class));
        findViewById(R.id.btn_clear_recent).setOnClickListener(view -> {
            search.setText("");
            applySearchFilter("");
        });

        if (PermissionsHelper.hasMediaPermissions(this)) {
            loadLocalMedia();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    private void loadLocalMedia() {
        LocalMediaRepository.get().load(this, (songs, videos) -> {
            allSongs.clear();
            allSongs.addAll(songs);
            allVideos.clear();
            allVideos.addAll(videos);
            applySearchFilter("");
        });
    }

    private void applySearchFilter(String query) {
        List<Song> songs = LocalMediaRepository.get().filterSongs(query);
        List<VideoItem> videos = LocalMediaRepository.get().filterVideos(query);
        songAdapter.submitSongs(LocalMediaRepository.preview(songs, 40));
        videoAdapter.submitVideos(LocalMediaRepository.preview(videos, 40));
    }

    private void playSong(Song song, int position) {
        int index = LocalMediaRepository.indexOfSong(allSongs, song);
        if (index < 0) {
            return;
        }
        PlaybackLauncher.playSongs(this, allSongs, index);
    }

    private void playVideo(VideoItem video, int position) {
        int index = LocalMediaRepository.indexOfVideo(allVideos, video);
        if (index < 0) {
            return;
        }
        PlaybackLauncher.playVideos(this, allVideos, index);
    }

    private boolean mediaPermissionGranted(Map<String, Boolean> grants) {
        if (PermissionsHelper.hasMediaPermissions(this)) {
            return true;
        }
        for (Boolean value : grants.values()) {
            if (Boolean.TRUE.equals(value)) {
                return true;
            }
        }
        return false;
    }
}

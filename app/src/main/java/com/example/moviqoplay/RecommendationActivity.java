package com.example.moviqoplay;

import android.os.Bundle;
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
import java.util.Map;

public class RecommendationActivity extends BaseActivity {
    private final ArrayList<Song> localSongs = new ArrayList<>();
    private final ArrayList<VideoItem> localVideos = new ArrayList<>();
    private SongAdapter firstSongs;
    private SongAdapter moreSongs;
    private VideoAdapter localVideosAdapter;

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
        setContentView(R.layout.activity_recommendation);
        applyTopInset(R.id.recommendation_scroll);

        RecyclerView songsOne = findViewById(R.id.rv_adaptive_1);
        RecyclerView songsTwo = findViewById(R.id.rv_adaptive_2);
        RecyclerView videos = findViewById(R.id.rv_adaptive_3);

        setupHorizontalList(songsOne);
        setupHorizontalList(songsTwo);
        setupHorizontalList(videos);

        firstSongs = new SongAdapter(this::playSong, true);
        moreSongs = new SongAdapter(this::playSong, true);
        localVideosAdapter = new VideoAdapter(this::playVideo, true);
        songsOne.setAdapter(firstSongs);
        songsTwo.setAdapter(moreSongs);
        videos.setAdapter(localVideosAdapter);

        Runnable playFirstSong = () -> {
            if (localSongs.isEmpty()) {
                Toast.makeText(this, R.string.no_local_songs, Toast.LENGTH_SHORT).show();
                return;
            }
            PlaybackLauncher.playSongs(this, localSongs, 0);
        };
        findViewById(R.id.btn_play_ai_mix).setOnClickListener(view -> playFirstSong.run());
        findViewById(R.id.card_ai_hero).setOnClickListener(view -> playFirstSong.run());

        if (PermissionsHelper.hasMediaPermissions(this)) {
            loadLocalMedia();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    private void loadLocalMedia() {
        LocalMediaRepository.get().load(this, (songs, videos) -> {
            localSongs.clear();
            localSongs.addAll(songs);
            localVideos.clear();
            localVideos.addAll(videos);

            int half = Math.max(1, Math.min(20, songs.size() / 2));
            firstSongs.submitSongs(LocalMediaRepository.preview(songs, half));
            if (songs.size() > half) {
                moreSongs.submitSongs(new ArrayList<>(songs.subList(half, Math.min(songs.size(), half * 2))));
            } else {
                moreSongs.submitSongs(LocalMediaRepository.preview(songs, half));
            }
            localVideosAdapter.submitVideos(LocalMediaRepository.preview(videos, 30));
        });
    }

    private void playSong(Song song, int position) {
        int index = LocalMediaRepository.indexOfSong(localSongs, song);
        if (index < 0) {
            return;
        }
        PlaybackLauncher.playSongs(this, localSongs, index);
    }

    private void playVideo(VideoItem video, int position) {
        int index = LocalMediaRepository.indexOfVideo(localVideos, video);
        if (index < 0) {
            return;
        }
        PlaybackLauncher.playVideos(this, localVideos, index);
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

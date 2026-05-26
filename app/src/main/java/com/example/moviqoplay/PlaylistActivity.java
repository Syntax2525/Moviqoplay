package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.PlaylistAdapter;
import com.example.moviqoplay.model.PlaylistItem;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.LocalMediaRepository;
import com.example.moviqoplay.util.PlaybackLauncher;
import com.example.moviqoplay.util.PermissionsHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaylistActivity extends BaseActivity {
    private final ArrayList<Song> allSongs = new ArrayList<>();
    private final ArrayList<com.example.moviqoplay.model.VideoItem> allVideos = new ArrayList<>();

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (mediaPermissionGranted(grants)) {
                    loadPlaylists();
                } else {
                    Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist);
        applyTopInset(R.id.playlist_content);
        applyFabBottomMargin(R.id.fab_create_playlist);

        TextView count = findViewById(R.id.txt_playlist_count);
        RecyclerView playlists = findViewById(R.id.rv_playlists);
        ExtendedFloatingActionButton create = findViewById(R.id.fab_create_playlist);

        setupVerticalList(playlists);
        PlaylistAdapter adapter = new PlaylistAdapter(new ArrayList<>(), item -> openPlaylist(item));
        playlists.setAdapter(adapter);

        create.setOnClickListener(view -> {
            if (allSongs.isEmpty()) {
                Toast.makeText(this, R.string.no_local_songs, Toast.LENGTH_SHORT).show();
                return;
            }
            PlaybackLauncher.playSongs(this, allSongs, 0);
        });

        if (PermissionsHelper.hasMediaPermissions(this)) {
            loadPlaylists();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    private void loadPlaylists() {
        LocalMediaRepository.get().load(this, (songs, videos) -> {
            allSongs.clear();
            allSongs.addAll(songs);
            allVideos.clear();
            allVideos.addAll(videos);

            List<PlaylistItem> items = LocalMediaRepository.get().buildPlaylistItems(this);
            TextView count = findViewById(R.id.txt_playlist_count);
            count.setText(getString(R.string.playlist_track_count, songs.size()));

            RecyclerView playlists = findViewById(R.id.rv_playlists);
            playlists.setAdapter(new PlaylistAdapter(items, this::openPlaylist));
        });
    }

    private void openPlaylist(PlaylistItem item) {
        if (item.isAllVideos()) {
            PlaybackLauncher.playVideos(this, allVideos, 0);
            return;
        }
        List<Song> queue = LocalMediaRepository.get().songsForPlaylist(this, item.getKey());
        if (queue.isEmpty()) {
            Toast.makeText(this, R.string.no_local_songs, Toast.LENGTH_SHORT).show();
            return;
        }
        PlaybackLauncher.playSongs(this, queue, 0);
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

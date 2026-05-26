package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.SongAdapter;
import com.example.moviqoplay.model.Song;
import com.example.moviqoplay.util.LocalMediaRepository;
import com.example.moviqoplay.util.PlaybackLauncher;
import com.example.moviqoplay.util.PermissionsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    private final ArrayList<Song> favoriteSongs = new ArrayList<>();
    private SongAdapter favoritesAdapter;

    private final ActivityResultLauncher<String[]> mediaPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (mediaPermissionGranted(grants)) {
                    loadFavorites();
                } else {
                    Toast.makeText(this, R.string.permission_denied_media, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        applyTopInset(R.id.profile_scroll);

        TextView profileName = findViewById(R.id.txt_profile_name);
        RecyclerView favorites = findViewById(R.id.rv_achievements);

        profileName.setText(R.string.local_listener);
        setupHorizontalList(favorites);
        favoritesAdapter = new SongAdapter(this::playSong, true);
        favorites.setAdapter(favoritesAdapter);

        findViewById(R.id.badge_premium).setOnClickListener(view -> openScreen(SettingsActivity.class));
        findViewById(R.id.img_profile_avatar).setOnClickListener(view -> openScreen(SettingsActivity.class));

        if (PermissionsHelper.hasMediaPermissions(this)) {
            loadFavorites();
        } else {
            PermissionsHelper.requestMediaPermissions(this, mediaPermissionLauncher);
        }
    }

    private void loadFavorites() {
        LocalMediaRepository.get().load(this, (songs, videos) -> {
            favoriteSongs.clear();
            favoriteSongs.addAll(LocalMediaRepository.get().favoriteSongs(this));
            favoritesAdapter.submitSongs(favoriteSongs);
        });
    }

    private void playSong(Song song, int position) {
        int index = LocalMediaRepository.indexOfSong(favoriteSongs, song);
        if (index < 0) {
            return;
        }
        PlaybackLauncher.playSongs(this, favoriteSongs, index);
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

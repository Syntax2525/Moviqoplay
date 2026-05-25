package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.PlaylistAdapter;
import com.example.moviqoplay.data.SampleData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PlaylistActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist);

        TextView count = findViewById(R.id.txt_playlist_count);
        RecyclerView playlists = findViewById(R.id.rv_playlists);
        FloatingActionButton create = findViewById(R.id.fab_create_playlist);

        count.setText(String.valueOf(SampleData.playlists().size()));
        setupVerticalList(playlists);
        playlists.setAdapter(new PlaylistAdapter(SampleData.playlists(), item -> openScreen(PlayerActivity.class)));
        create.setOnClickListener(view -> openScreen(PlayerActivity.class));
    }
}

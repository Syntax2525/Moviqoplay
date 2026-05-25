package com.example.moviqoplay;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnOpenHome).setOnClickListener(view -> openScreen(HomeActivity.class));
        findViewById(R.id.btnOpenPlayer).setOnClickListener(view -> openScreen(PlayerActivity.class));
        findViewById(R.id.btnOpenVideo).setOnClickListener(view -> openScreen(VideoActivity.class));
        findViewById(R.id.btnOpenPlaylist).setOnClickListener(view -> openScreen(PlaylistActivity.class));
        findViewById(R.id.btnOpenEqualizer).setOnClickListener(view -> openScreen(EqualizerActivity.class));
    }
}

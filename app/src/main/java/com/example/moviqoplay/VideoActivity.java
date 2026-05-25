package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.MediaAdapter;
import com.example.moviqoplay.data.SampleData;
import com.google.android.material.slider.Slider;

public class VideoActivity extends BaseActivity {
    private boolean playing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video);

        ImageButton back = findViewById(R.id.btn_video_back);
        ImageButton play = findViewById(R.id.btn_video_play);
        Slider seekVideo = findViewById(R.id.seek_video);
        TextView title = findViewById(R.id.txt_video_title);
        RecyclerView recommendedVideos = findViewById(R.id.rv_recommended_videos);

        title.setText(R.string.sample_video_title);
        seekVideo.setValue(44);
        setupHorizontalList(recommendedVideos);
        recommendedVideos.setAdapter(new MediaAdapter(SampleData.videoItems(), item -> openScreen(VideoActivity.class)));

        back.setOnClickListener(view -> finish());
        play.setOnClickListener(view -> playing = !playing);
        findViewById(R.id.btn_subtitles).setOnClickListener(view -> openScreen(SettingsActivity.class));
        findViewById(R.id.btn_quality).setOnClickListener(view -> openScreen(SettingsActivity.class));
        findViewById(R.id.btn_audio_track).setOnClickListener(view -> openScreen(EqualizerActivity.class));
    }
}

package com.example.moviqoplay;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.MediaAdapter;
import com.example.moviqoplay.data.SampleData;

public class RecommendationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recommendation);

        RecyclerView adaptiveOne = findViewById(R.id.rv_adaptive_1);
        RecyclerView adaptiveTwo = findViewById(R.id.rv_adaptive_2);
        RecyclerView adaptiveThree = findViewById(R.id.rv_adaptive_3);

        setupHorizontalList(adaptiveOne);
        setupHorizontalList(adaptiveTwo);
        setupHorizontalList(adaptiveThree);
        adaptiveOne.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class)));
        adaptiveTwo.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class)));
        adaptiveThree.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(VideoActivity.class)));
        findViewById(R.id.btn_play_ai_mix).setOnClickListener(view -> openScreen(PlayerActivity.class));
        findViewById(R.id.card_ai_hero).setOnClickListener(view -> openScreen(PlayerActivity.class));
    }
}

package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.MediaAdapter;
import com.example.moviqoplay.data.SampleData;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        EditText search = findViewById(R.id.edit_search);
        ImageButton voice = findViewById(R.id.btn_voice_search);
        RecyclerView trendingSearches = findViewById(R.id.rv_trending_searches);
        RecyclerView recentSearches = findViewById(R.id.rv_recent_searches);

        setupHorizontalList(trendingSearches);
        setupHorizontalList(recentSearches);
        trendingSearches.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class)));
        recentSearches.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class)));

        search.setOnEditorActionListener((view, actionId, event) -> {
            openScreen(PlayerActivity.class);
            return true;
        });
        voice.setOnClickListener(view -> openScreen(RecommendationActivity.class));
        findViewById(R.id.btn_clear_recent).setOnClickListener(view -> recentSearches.setAdapter(
                new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class))
        ));
    }
}

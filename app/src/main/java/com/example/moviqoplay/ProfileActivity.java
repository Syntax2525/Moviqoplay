package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.adapter.MediaAdapter;
import com.example.moviqoplay.data.SampleData;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        TextView profileName = findViewById(R.id.txt_profile_name);
        RecyclerView achievements = findViewById(R.id.rv_achievements);

        profileName.setText(R.string.sample_profile_name);
        setupHorizontalList(achievements);
        achievements.setAdapter(new MediaAdapter(SampleData.mediaItems(), item -> openScreen(PlayerActivity.class)));
        findViewById(R.id.badge_premium).setOnClickListener(view -> openScreen(SettingsActivity.class));
        findViewById(R.id.img_profile_avatar).setOnClickListener(view -> openScreen(SettingsActivity.class));
    }
}

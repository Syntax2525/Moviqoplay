package com.example.moviqoplay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;

public class MainActivity extends BaseActivity {
    private static final long TAP_ANIMATION_MS = 120L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        applyTopInset(R.id.dashboardScroll);
        applyBottomScrollPadding(R.id.dashboardScroll, 0);
        bindDashboardCard(R.id.cardHome, HomeActivity.class);
        bindDashboardCard(R.id.cardMusic, PlayerActivity.class);
        bindDashboardCard(R.id.cardVideo, VideoPlayerActivity.class);
        bindDashboardCard(R.id.cardPlaylist, PlaylistActivity.class);
        bindDashboardCard(R.id.cardSearch, SearchActivity.class);
        bindDashboardCard(R.id.cardEqualizer, EqualizerActivity.class);
        bindDashboardCard(R.id.cardSettings, SettingsActivity.class);
        bindDashboardCard(R.id.cardProfile, ProfileActivity.class);
        bindDashboardCard(R.id.cardRecommendation, RecommendationActivity.class);
    }

    private void bindDashboardCard(int viewId, Class<? extends Activity> destination) {
        View card = findViewById(viewId);
        card.setOnClickListener(view -> animateThenOpen(view, destination));
    }

    private void animateThenOpen(View view, Class<? extends Activity> destination) {
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .alpha(0.92f)
                .setDuration(TAP_ANIMATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(TAP_ANIMATION_MS)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> openScreen(destination))
                        .start())
                .start();
    }
}

package com.example.moviqoplay;

import android.content.Intent;
import android.os.Bundle;

public class VideoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, VideoPlayerActivity.class));
        finish();
    }
}

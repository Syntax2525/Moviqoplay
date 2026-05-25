package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        MaterialSwitch amoled = findViewById(R.id.switch_amoled);
        MaterialSwitch glass = findViewById(R.id.switch_glass);
        MaterialSwitch gapless = findViewById(R.id.switch_gapless);
        Slider crossfade = findViewById(R.id.seek_crossfade);
        RadioGroup downloadQuality = findViewById(R.id.group_download_quality);

        amoled.setOnCheckedChangeListener((buttonView, isChecked) -> glass.setEnabled(isChecked));
        glass.setOnCheckedChangeListener((buttonView, isChecked) -> crossfade.setEnabled(gapless.isChecked()));
        gapless.setOnCheckedChangeListener((buttonView, isChecked) -> crossfade.setEnabled(isChecked));
        downloadQuality.setOnCheckedChangeListener((group, checkedId) -> findViewById(R.id.switch_releases).setEnabled(true));
        findViewById(R.id.btn_manage_account).setOnClickListener(view -> openScreen(ProfileActivity.class));
    }
}

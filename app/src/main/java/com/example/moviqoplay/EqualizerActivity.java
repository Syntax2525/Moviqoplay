package com.example.moviqoplay;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class EqualizerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_equalizer);
        applyTopInset(R.id.equalizer_scroll);

        MaterialSwitch enabled = findViewById(R.id.switch_eq_enabled);
        Slider bass = findViewById(R.id.seek_bass);
        Slider virtualizer = findViewById(R.id.seek_virtualizer);
        Slider reverb = findViewById(R.id.seek_reverb);
        Chip cinematic = findViewById(R.id.preset_cinematic);

        enabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            bass.setEnabled(isChecked);
            virtualizer.setEnabled(isChecked);
            reverb.setEnabled(isChecked);
        });
        cinematic.setOnClickListener(view -> {
            bass.setValue(82);
            virtualizer.setValue(70);
            reverb.setValue(55);
        });
    }
}

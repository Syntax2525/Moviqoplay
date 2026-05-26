package com.example.moviqoplay;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends BaseActivity {
    private RadioGroup themeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        applyTopInset(R.id.settings_scroll);

        MaterialSwitch gapless = findViewById(R.id.switch_gapless);
        Slider crossfade = findViewById(R.id.seek_crossfade);
        RadioGroup downloadQuality = findViewById(R.id.group_download_quality);
        themeGroup = findViewById(R.id.group_theme);

        setupThemeSelector();
        gapless.setOnCheckedChangeListener((buttonView, isChecked) -> crossfade.setEnabled(isChecked));
        downloadQuality.setOnCheckedChangeListener((group, checkedId) -> findViewById(R.id.switch_releases).setEnabled(true));
        findViewById(R.id.btn_manage_account).setOnClickListener(view -> openScreen(ProfileActivity.class));
    }

    private void setupThemeSelector() {
        String savedTheme = ThemeManager.getSavedTheme(this);
        if (ThemeManager.THEME_DARK.equals(savedTheme)) {
            themeGroup.check(R.id.theme_dark);
        } else if (ThemeManager.THEME_LIGHT.equals(savedTheme)) {
            themeGroup.check(R.id.theme_light);
        } else {
            themeGroup.check(R.id.theme_system);
        }

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String theme;
            if (checkedId == R.id.theme_dark) {
                theme = ThemeManager.THEME_DARK;
            } else if (checkedId == R.id.theme_light) {
                theme = ThemeManager.THEME_LIGHT;
            } else {
                theme = ThemeManager.THEME_SYSTEM;
            }
            updateRadioStates(checkedId);
            ThemeManager.saveAndApplyTheme(this, theme);
        });
        updateRadioStates(themeGroup.getCheckedRadioButtonId());
    }

    private void updateRadioStates(int checkedId) {
        setRadioSelected(R.id.theme_dark, checkedId);
        setRadioSelected(R.id.theme_light, checkedId);
        setRadioSelected(R.id.theme_system, checkedId);
    }

    private void setRadioSelected(int radioId, int checkedId) {
        MaterialRadioButton button = findViewById(radioId);
        button.setSelected(radioId == checkedId);
    }
}

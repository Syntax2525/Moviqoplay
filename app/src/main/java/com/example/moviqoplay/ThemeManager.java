package com.example.moviqoplay;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_SYSTEM = "system";

    private static final String PREFS_NAME = "movoqoplay_theme";
    private static final String KEY_THEME = "selected_theme";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(modeForTheme(getSavedTheme(context)));
    }

    public static void saveAndApplyTheme(Context context, String theme) {
        saveTheme(context, theme);
        AppCompatDelegate.setDefaultNightMode(modeForTheme(theme));
    }

    public static String getSavedTheme(Context context) {
        return preferences(context).getString(KEY_THEME, THEME_SYSTEM);
    }

    public static int modeForTheme(String theme) {
        if (THEME_DARK.equals(theme)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        } else if (THEME_LIGHT.equals(theme)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private static void saveTheme(Context context, String theme) {
        preferences(context).edit().putString(KEY_THEME, theme).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

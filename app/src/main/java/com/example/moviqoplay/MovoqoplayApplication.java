package com.example.moviqoplay;

import android.app.Application;

public class MovoqoplayApplication extends Application {
    @Override
    public void onCreate() {
        ThemeManager.applySavedTheme(this);
        super.onCreate();
    }
}

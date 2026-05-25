package com.example.moviqoplay.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

public final class PermissionsHelper {
    private PermissionsHelper() {
    }

    public static String audioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_AUDIO;
        }
        return Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, audioPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAudioPermission(Activity activity, ActivityResultLauncher<String> launcher) {
        launcher.launch(audioPermission());
    }
}

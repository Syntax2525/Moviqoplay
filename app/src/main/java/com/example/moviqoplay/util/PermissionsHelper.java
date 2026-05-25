package com.example.moviqoplay.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

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

    public static String videoPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_VIDEO;
        }
        return Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    public static boolean hasVideoPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, videoPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasMediaPermissions(Context context) {
        return hasAudioPermission(context) && hasVideoPermission(context);
    }

    public static String[] mediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    public static String[] missingMediaPermissions(Context context) {
        List<String> permissions = new ArrayList<>();
        for (String permission : mediaPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }
        return permissions.toArray(new String[0]);
    }

    public static void requestAudioPermission(Activity activity, ActivityResultLauncher<String> launcher) {
        launcher.launch(audioPermission());
    }

    public static void requestMediaPermissions(Activity activity, ActivityResultLauncher<String[]> launcher) {
        launcher.launch(missingMediaPermissions(activity));
    }
}

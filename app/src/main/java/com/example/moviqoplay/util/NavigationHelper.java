package com.example.moviqoplay.util;

import android.app.Activity;
import android.content.Intent;

public final class NavigationHelper {
    private NavigationHelper() {
    }

    public static void open(Activity activity, Class<? extends Activity> destination) {
        activity.startActivity(new Intent(activity, destination));
    }
}

package com.example.moviqoplay;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.util.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        applySystemBars();
    }

    protected void applySystemBars() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black_amoled));
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!isNightModeActive());
        controller.setAppearanceLightNavigationBars(!isNightModeActive());
    }

    protected boolean isNightModeActive() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    protected void openScreen(Class<? extends Activity> destination) {
        NavigationHelper.open(this, destination);
    }

    protected void setupHorizontalList(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
    }

    protected void setupVerticalList(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    /** Applies status-bar top inset to a scrollable or padded content root. */
    protected void applyTopInset(@IdRes int contentRootId) {
        View content = findViewById(contentRootId);
        if (content == null) {
            return;
        }
        final int initialTop = content.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    initialTop + bars.top,
                    view.getPaddingRight(),
                    view.getPaddingBottom());
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(content);
    }

    /** Adds bottom safe-area padding to scroll content and FAB-style overlays. */
    protected void applyBottomScrollPadding(@IdRes int scrollId, int extraBottomDp) {
        View scroll = findViewById(scrollId);
        if (scroll == null) {
            return;
        }
        final int initialBottom = scroll.getPaddingBottom();
        final int extraPx = (int) (extraBottomDp * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    initialBottom + bars.bottom + extraPx);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(scroll);
    }

    /**
     * Home screen: bottom bar flush to screen edge with nav-bar padding; scroll clears the bar only.
     */
    protected void applyHomeBottomBarInsets(@IdRes int bottomBarId, @IdRes int scrollId) {
        View bottomBar = findViewById(bottomBarId);
        View scroll = findViewById(scrollId);
        if (bottomBar == null || scroll == null) {
            return;
        }

        final int initialScrollTop = scroll.getPaddingTop();
        final int initialScrollBottom = scroll.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(bottomBar, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    bars.bottom);
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(scroll, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    initialScrollTop + bars.top,
                    view.getPaddingRight(),
                    initialScrollBottom);
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(bottomBar);
        ViewCompat.requestApplyInsets(scroll);
    }

    protected void applyFabBottomMargin(@IdRes int fabId) {
        View fab = findViewById(fabId);
        if (fab == null) {
            return;
        }
        final int initialBottom = getResources().getDimensionPixelSize(R.dimen.space_lg);
        ViewCompat.setOnApplyWindowInsetsListener(fab, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lp.bottomMargin = initialBottom + bars.bottom;
            view.setLayoutParams(lp);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(fab);
    }
}

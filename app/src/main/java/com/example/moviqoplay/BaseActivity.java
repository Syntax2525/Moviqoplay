package com.example.moviqoplay;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviqoplay.util.NavigationHelper;

public abstract class BaseActivity extends AppCompatActivity {

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
}

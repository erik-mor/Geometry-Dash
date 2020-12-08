package com.example.geometry_dash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class SelectLevelActivity extends AppCompatActivity {
    ListView levelList;

    String[] levels = {"Level 1", "Level 2"};
    int[] progress = {50, 25};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        levelList = (ListView) findViewById(R.id.levelList);

        LevelAdapter adapter =  new LevelAdapter(SelectLevelActivity.this, levels, progress);
        levelList.setAdapter(adapter);

        levelList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("level", i);
            startActivity(intent);
        });
    }
}
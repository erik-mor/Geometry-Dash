package com.example.geometry_dash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.io.IOException;
import java.io.InputStream;

public class SelectLevelActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    ListView levelList;
    String[] levels;
    MediaPlayer playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        playSound = MediaPlayer.create(this, R.raw.play);

        levelList = (ListView) findViewById(R.id.levelList);
        sharedPreferences = getSharedPreferences("appData", MODE_PRIVATE);

        // get levels from assets
        AssetManager assetManager = getAssets();
        InputStream input;
        try {
            input = assetManager.open("levels.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String txt = new String(buffer);

            // each level is on new line
            levels = txt.split("\\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize custom list adapter to display levels
        LevelAdapter adapter =  new LevelAdapter(SelectLevelActivity.this, levels.length);
        levelList.setAdapter(adapter);
        levelList.setOnItemClickListener((adapterView, view, i, l) -> {
            playSound.start();
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("level", i);
            intent.putExtra("obstacles", levels[i]);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
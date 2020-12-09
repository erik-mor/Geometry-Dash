package com.example.geometry_dash;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        int level = getIntent().getIntExtra("level", 0);
        String[] obstacles = getIntent().getStringExtra("obstacles").split("");
        int[] intObstacles = new int[obstacles.length - 1];
        for (int i = 0; i < intObstacles.length; i++) {
            intObstacles[i] = Integer.parseInt(obstacles[i + 1]);
        }

        gameView = new GameView(this, point.x, point.y, level, intObstacles);

        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}
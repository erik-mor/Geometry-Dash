package com.example.geometry_dash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {

    private int JUMP_STEP = 40;
    private int SPEED = 25;

    private Thread thread;
    private boolean isPlaying;
    private final int screenX, screenY;
    private float screenRationX, screenRationY;
    private Paint paint;
    private Paint rectPaint;
    private final Background background1, background2;
    private boolean isJumping;
    private int jumpState;
    private final Rect rect;
    private List<Obstacle> obstacleList;


    public GameView(Context context, int screenX, int screenY) {
        super(context);

        this.screenX = screenX;
        this.screenY = screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.x = screenX;

        paint = new Paint();
        rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStrokeWidth(5);

        rect = new Rect(120, 550, 220 ,655);

        isJumping = false;
        jumpState = 0;

        obstacleList = new ArrayList<>();

        Obstacle obstacle1 = new Obstacle(800, 655, 70, 100);
        obstacleList.add(obstacle1);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {
        background1.x -= SPEED;
        background2.x -= SPEED;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        for (Obstacle o: obstacleList) {
            o.setX(o.getX() - SPEED);
            if (o.getX() <= 0) {
                o.setX(screenX - o.getWidth());
            }
        }


        if (isJumping) {
            jumpState++;
            if (jumpState > 10) {
                isJumping = false;
                jumpState = 0;
            } else if (jumpState > 5) {
                rect.top += JUMP_STEP;
                rect.bottom += JUMP_STEP;
            } else {
                rect.top -= JUMP_STEP;
                rect.bottom -= JUMP_STEP;
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            canvas.drawRect(rect, rectPaint);
            for (Obstacle o: obstacleList) {
                o.draw(canvas, rectPaint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isJumping) {
                isJumping = true;
            }
        }

        return super.onTouchEvent(event);
    }
}
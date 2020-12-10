package com.example.geometry_dash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceView;

import static android.content.Context.MODE_PRIVATE;

public class GameView extends SurfaceView implements Runnable {
    private final int[] backgroundImages = {R.drawable.level1, R.drawable.level2};
    private final int[] levelsMusic = {R.raw.level1, R.raw.level2};

    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private MediaPlayer levelMusic;
    private MediaPlayer explode;
    private MediaPlayer endMusic;

    private final int JUMP_STEP = 20;
    private final int N_JUMP_STEPS = 10;
    private final int SPEED = 20;
    private final int RECTANGLE_TOP = 540;
    private final int BOTTOM = 640;

    private final int screenX, screenY;

    private final Paint blickPaint;
    private final Paint paint;
    private final Paint rectPaint;
    private final Paint linePaint;
    private final Paint textPaint;
    private final Paint menuPaint;

    private Thread thread;
    private final Background background1, background2;
    private final Rect rect;
    private Obstacle obstacle;

    private boolean blick;
    private boolean isFinished;
    private boolean isPlaying;
    private boolean isJumping;
    private int jumpState;
    private final int[] obstacles;
    private final int level;
    private int current;
    private int score = 0;

    public GameView(Context context, int screenX, int screenY, int level, int[] obstacles) {
        super(context);

        sharedPreferences = getContext().getSharedPreferences("appData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        levelMusic = MediaPlayer.create(getContext(), levelsMusic[level]);
        explode = MediaPlayer.create(getContext(), R.raw.explode);
        endMusic = MediaPlayer.create(getContext(), R.raw.end);

        this.screenX = screenX;
        this.screenY = screenY;

        this.level = level;
        background1 = new Background(screenX, screenY, getResources(), backgroundImages[level]);
        background2 = new Background(screenX, screenY, getResources(), backgroundImages[level]);
        background2.x = screenX;

        paint = new Paint();
        rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStrokeWidth(5);

        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(5);

        textPaint = new Paint();
        textPaint.setColor(Color.LTGRAY);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(60);
        textPaint.setStrokeWidth(6);

        menuPaint = new Paint();
        menuPaint.setColor(Color.LTGRAY);
        menuPaint.setAntiAlias(true);
        menuPaint.setTextSize(30);
        menuPaint.setStrokeWidth(4);

        blickPaint = new Paint();
        blickPaint.setStrokeWidth(25);

        rect = new Rect(150, RECTANGLE_TOP, 250 , BOTTOM);

        blick = false;
        isFinished = false;
        isJumping = false;
        jumpState = 0;

        this.obstacles = obstacles;
        current= 0;
        obstacle = new Obstacle(screenX - 100, 640, obstacles[current]);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            if (!isFinished) checkForCollision();
            sleep();
        }
    }

    private void update() {
        // move background
        if (!isFinished) {
            background1.x -= SPEED;
            background2.x -= SPEED;

            // move background images to simulate movement
            if (background1.x + background1.background.getWidth() < 0) {
                background1.x = screenX;
            }

            if (background2.x + background2.background.getWidth() < 0) {
                background2.x = screenX;
            }

            // move obstacle
            obstacle.setX(obstacle.getX() - SPEED);

            // if obstacle is out of screen, move it to the start
            int margin = obstacle.type == 1 ? obstacle.WIDTH : obstacle.WIDTH * 2;
            if (obstacle.getX() + margin <= 0) {
                current++;
                // if it was last obstacle game is finished
                if (current >= obstacles.length) {
                    isFinished = true;
                // else get next obstacle
                } else {
                    obstacle.setType(obstacles[current]);
                    obstacle.setX(screenX - obstacle.WIDTH);
                }
            }
        // if game is finished successfully move rectangle into the gate on right side of screen
        } else {
            if (rect.left <= screenX) {
                rect.left += SPEED;
                rect.right += SPEED;
            } else {
                end();
            }
        }

        // move rectangle up or down based on jump state
        if (isJumping) {
            jumpState++;
            // back on ground
            if (jumpState > (2 * N_JUMP_STEPS)) {
                isJumping = false;
                jumpState = 0;

            // going down
            } else if (jumpState > N_JUMP_STEPS) {
                rect.top += JUMP_STEP;
                rect.bottom += JUMP_STEP;

            // going up
            } else {
                rect.top -= JUMP_STEP;
                rect.bottom -= JUMP_STEP;
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            // get canvas
            Canvas canvas = getHolder().lockCanvas();

            // draw moving background image and bottom line
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);
            canvas.drawLine(0, 640, screenX, 640, linePaint);

            // if game finished successfully draw end blicking gate or else draw obstacle
            if (!isFinished) {
                obstacle.draw(canvas, rectPaint);
            } else {
                if (blick) blickPaint.setColor(Color.LTGRAY);
                else blickPaint.setColor(Color.DKGRAY);
                blick = !blick;
                canvas.drawLine(screenX, 0, screenX, screenY, blickPaint);
            }

            // if game ends draw info text and menu button
            if (isPlaying) {
                canvas.drawRect(rect, rectPaint);
            } else {
                if (isFinished) {
                    canvas.drawText("Level finished !!!", 350, screenY >> 1, textPaint);
                } else {
                    canvas.drawText("Game over. Progress: " + score + "%", 250, screenY >> 1, textPaint);
                }
                canvas.drawText("Menu", 10, 35, menuPaint);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void checkForCollision() {

        // if in middle of jump check check if left or right bottom corner is in are of obstacle
        // if yes check if it actually collides with obstacle
        if (isJumping) {
            if (obstacle.isInArea(rect.right, rect.bottom) && obstacle.frontCollision(rect)) collision();
            else if(obstacle.isInArea(rect.left, rect.bottom) && obstacle.backCollision(rect)) collision();
        } else {
            if (obstacle.xIsInArea(rect.right)) collision();
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
        levelMusic.start();
    }

    private void restart() {
        rect.set(150, RECTANGLE_TOP, 250 , BOTTOM);
        current = 0;
        obstacle = new Obstacle(screenX - 100, 640, obstacles[current]);
        isJumping = false;
        jumpState = 0;
        resume();
    }

    public void pause() {
        try {
            isPlaying = false;
            levelMusic.stop();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // when game ends successfully
    private void end() {
        isPlaying = false;
        endMusic.start();
        levelMusic.stop();
        draw();
        setProgress();
    }

    // ved collision happens print info on screen and stop music
    private void collision() {
        if (explode.isPlaying()) {
            explode.pause();
            explode.seekTo(0);
        }
        explode.start();
        levelMusic.pause();
        levelMusic.seekTo(0);
        setProgress();
        isPlaying = false;
        rect.setEmpty();
        draw();
    }

    // get current best progress from shared pref and if it is smaller then update
    private void setProgress() {
        score = (100 * current) / obstacles.length;
        int savedProgress = sharedPreferences.getInt("level" + level + "-progress", 0);
        if (savedProgress < score) {
            editor.putInt("level" + level + "-progress", score);
            editor.commit();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // jump
            if (isPlaying) {
                if (!isJumping) {
                    isJumping = true;
                }
            } else {
                // when game ended and player taps on the screen
                // if level is finished go to main activity
                if (isFinished) {
                    pause();
                    getContext().startActivity(new Intent(getContext(), MainActivity.class));
                // if level ended in collision go again
                } else {
                    if (event.getX() <= 100 && event.getY() <= 100) {
                        pause();
                        getContext().startActivity(new Intent(getContext(), MainActivity.class));
                    } else {
                        restart();
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }
}
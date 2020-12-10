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
    private int SPEED = 20;

    private int RECTANGLE_TOP = 540;
    private int RECTANGLE_BOTTOM = 640;

    private Thread thread;

    private final int screenX, screenY;
    private float screenRationX, screenRationY;

    private Paint blickPaint;
    private Paint paint;
    private Paint rectPaint;
    private Paint linePaint;
    private Paint textPaint;
    private Paint menuPaint;

    private final Background background1, background2;

    private boolean blick;
    private boolean isFinished;
    private boolean isPlaying;
    private boolean isJumping;
    private int jumpState;
    private final Rect rect;
//    private final RectF rect;
//    private final Matrix rectMatrix;
    private Obstacle obstacle;
    private final int[] obstacles;
    private int current;
    private int level;
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

        rect = new Rect(150, RECTANGLE_TOP, 250 ,RECTANGLE_BOTTOM);
//        rect = new RectF(120, 550, 220 ,655);
//        rectMatrix = new  Matrix();

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
                if (current >= obstacles.length) {
//                System.out.println("Song is at: " + levelMusic.getCurrentPosition());
//                collision();
                    isFinished = true;
                } else {
                    obstacle.setType(obstacles[current]);
                    obstacle.setX(screenX - obstacle.WIDTH);
                }
            }
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
//                rectMatrix.setRotate(jumpState * 36, rect.centerX(), rect.centerY());
//                rectMatrix.mapRect(rect);

            // going up
            } else {
                rect.top -= JUMP_STEP;
                rect.bottom -= JUMP_STEP;
//                rectMatrix.setRotate(jumpState * 36, rect.centerX(), rect.centerY());
//                rectMatrix.mapRect(rect);
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);
            canvas.drawLine(0, 640, screenX, 640, linePaint);

//            if (isJumping) {
//                canvas.save();
//                canvas.rotate(jumpState * 36);
//                canvas.drawRect(rect, rectPaint);
//                canvas.restore();
//            } else {
//                canvas.drawRect(rect, rectPaint);
//            }
            canvas.drawRect(rect, rectPaint);

            if (!isFinished) {
                obstacle.draw(canvas, rectPaint);
            }

            if (isFinished) {
                if (blick) blickPaint.setColor(Color.LTGRAY);
                else blickPaint.setColor(Color.DKGRAY);
                blick = !blick;
                canvas.drawLine(screenX, 0, screenX, screenY, blickPaint);
            }

            if (!isPlaying) {
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
        rect.set(150, RECTANGLE_TOP, 250 ,RECTANGLE_BOTTOM);
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

    private void end() {
        isPlaying = false;
        rect.setEmpty();
        endMusic.start();
        levelMusic.stop();
        setProgress();
        draw();
    }

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
            if (isPlaying) {
                if (!isJumping) {
                    isJumping = true;
                }
            } else {
                if (isFinished) {
                    pause();
                    getContext().startActivity(new Intent(getContext(), MainActivity.class));
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
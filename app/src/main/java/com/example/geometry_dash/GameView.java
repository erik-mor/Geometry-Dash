package com.example.geometry_dash;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;

import static android.content.Context.MODE_PRIVATE;

public class GameView extends SurfaceView implements Runnable {
    private final int[] backgroundImages = {R.drawable.level1, R.drawable.level2};

    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final int JUMP_STEP = 20;
    private final int N_JUMP_STEPS = 10;
    private int SPEED = 20;

    private int RECTANGLE_TOP = 540;
    private int RECTANGLE_BOTTOM = 640;

    private Thread thread;
    private boolean isPlaying;
    private final int screenX, screenY;
    private float screenRationX, screenRationY;
    private Paint paint;
    private Paint rectPaint;
    private Paint linePaint;
    private final Background background1, background2;
    private boolean isJumping;
    private int jumpState;
    private final Rect rect;
//    private final RectF rect;
//    private final Matrix rectMatrix;
    private Obstacle obstacle;
    private final int[] obstacles;
    private int current;
    private int level;

    public GameView(Context context, int screenX, int screenY, int level, int[] obstacles) {
        super(context);

        sharedPreferences = getContext().getSharedPreferences("appData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

        rect = new Rect(150, RECTANGLE_TOP, 250 ,RECTANGLE_BOTTOM);
//        rect = new RectF(120, 550, 220 ,655);
//        rectMatrix = new Matrix();

        isJumping = false;
        jumpState = 0;

        this.obstacles = obstacles;
        current= 0;
        obstacle = new Obstacle(800, 640, obstacles[current]);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            checkForCollision();
            sleep();
        }
    }

    private void update() {
        // move background
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
                current = 0;
            }
            obstacle.setType(obstacles[current]);
            obstacle.setX(screenX - obstacle.WIDTH);
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

            obstacle.draw(canvas, rectPaint);

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
    }

    private void restart() {
        resume();
        rect.bottom = RECTANGLE_BOTTOM;
        rect.top = RECTANGLE_TOP;
        current = 0;
        obstacle = new Obstacle(800, 640, obstacles[current]);
        isJumping = false;
        jumpState = 0;
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void collision() {
        int progress = (100 * current) / obstacles.length;
        System.out.println(current);
        System.out.println(obstacles.length);
        System.out.println(progress);
        int savedProgress = sharedPreferences.getInt("level" + level + "-progress", 0);
        System.out.println("Level: " + level);
        System.out.println(savedProgress);
        if (savedProgress < progress) {
            editor.putInt("level" + level + "-progress", progress);
            editor.commit();
        }
        isPlaying = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isPlaying) {
                if (!isJumping) {
                    isJumping = true;
                }
            } else {
                restart();
            }
        }

        return super.onTouchEvent(event);
    }
}
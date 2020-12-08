package com.example.geometry_dash;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.List;

public class Obstacle {
    private int x;
    private int y;

    Point point1;
    Point point2;
    Point point3;
    Point point4;
    Point point5;
    Point point6;

    public int WIDTH = 70;
    public int HEIGHT = 100;
    public int type;

    public Obstacle(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;

        // initialize triangle points
        point1 = new Point();
        point2 = new Point();
        point3 = new Point();
        point4 = new Point();
        point5 = new Point();
        point6 = new Point();
    }

    public void draw(Canvas canvas, Paint paint) {
        point1.set(this.x, this.y);
        point2.set(this.x + this.WIDTH / 2, this.y - this.HEIGHT);
        point3.set(this.x + this.WIDTH, this.y);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1.x, point1.y);
        path.lineTo(point2.x, point2.y);
        path.lineTo(point3.x, point3.y);
        path.close();

        canvas.drawPath(path, paint);

        // if type is 2 draw another triangle next to first one
        if (type == 2) {
            point4.set(this.x + this.WIDTH, this.y);
            point5.set(point4.x + this.WIDTH / 2, this.y - this.HEIGHT);
            point6.set(point4.x + this.WIDTH, this.y);

            path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(point4.x, point4.y);
            path.lineTo(point5.x, point5.y);
            path.lineTo(point6.x, point6.y);
            path.close();

            canvas.drawPath(path, paint);
        }
    }

    // check if front corner point is inside triangle
    public boolean frontCollision(Rect rect) {
        return isLeft(point1, point2, rect.right, rect.bottom) <= 0;
    }

    // check if back corner point is inside triangle
    public boolean backCollision(Rect rect) {
        if (type == 1) return isLeft(point2, point3, rect.left, rect.bottom) <= 0;
        else return isLeft(point5, point6, rect.left, rect.bottom) <= 0;

    }

    // check if point x, y is inside rectangle around obstacles
    public boolean isInArea(int rectX, int rectY) {
        if (type == 1)
            return rectX >= point1.x && rectX < point3.x && rectY > point2.y;
        else {
            return rectX >= point1.x && rectX < point6.x && rectY > point2.y;
        }
    }

    // check if x coordinate of rectangle is inside triangle
    public boolean xIsInArea(int rectX) {
        if (type == 1)
            return rectX >= point1.x && rectX < point3.x;
        else {
            return rectX >= point1.x && rectX < point6.x;
        }
    }

    // compute cross product to determine on which side of line(from a to b) the point is -> to check for collision
    public static int isLeft(Point a, Point b, int rectX, int rectY){
        return ((b.x - a.x)*((rectY * -1) - (a.y * -1)) - ((b.y * -1) - (a.y * -1))*(rectX - a.x));
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setType(int type) {
        this.type = type;
    }
}

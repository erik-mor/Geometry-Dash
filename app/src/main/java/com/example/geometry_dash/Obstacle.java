package com.example.geometry_dash;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

public class Obstacle {
    public int WIDTH = 55;
    public int HEIGHT = 100;

    private int x;
    private int y;

    Point point1;
    Point point2;
    Point point3;
    Point point4;
    Point point5;
    Point point6;
    Point point7;
    Point point8;
    Point point9;

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
        point7 = new Point();
        point8 = new Point();
        point9 = new Point();

    }

    // draw triangle path
    public Path getPath(Point p1, Point p2, Point p3) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
        return path;
    }

    // drew obstacles based on type
    public void draw(Canvas canvas, Paint paint) {
        point1.set(this.x, this.y);
        point2.set(this.x + this.WIDTH / 2, this.y - this.HEIGHT);
        point3.set(this.x + this.WIDTH, this.y);
        canvas.drawPath(getPath(point1, point2, point3), paint);

        // if type is 2 draw another triangle next to first one
        if (type == 2) {
            point4.set(this.x + this.WIDTH, this.y);
            point5.set(point4.x + this.WIDTH / 2, this.y - this.HEIGHT);
            point6.set(point4.x + this.WIDTH, this.y);
            canvas.drawPath(getPath(point4, point5, point6), paint);
        }

        if (type == 3) {
            point4.set(point3.x, this.y);
            point5.set(point4.x + this.WIDTH / 2, this.y - this.HEIGHT);
            point6.set(point4.x + this.WIDTH, this.y);
            canvas.drawPath(getPath(point4, point5, point6), paint);

            point7.set(point6.x, this.y);
            point8.set(point7.x + this.WIDTH / 2, this.y - this.HEIGHT);
            point9.set(point7.x + this.WIDTH, this.y);

            canvas.drawPath(getPath(point7, point8, point9), paint);
        }
    }

    // check if front corner point is inside triangle
    public boolean frontCollision(Rect rect) {
        return crossProduct(point1, point2, rect.right, rect.bottom) <= 0;
    }

    // check if back corner point is inside triangle
    public boolean backCollision(Rect rect) {
        if (type == 1) return crossProduct(point2, point3, rect.left, rect.bottom) <= 0;
        else if (type == 2) return crossProduct(point5, point6, rect.left, rect.bottom) <= 0;
        else return crossProduct(point8, point9, rect.left, rect.bottom) <= 0;
    }

    // check if point x, y is inside rectangle around obstacles
    public boolean isInArea(int rectX, int rectY) {
        if (type == 1)
            return rectX >= point1.x && rectX < point3.x && rectY > point2.y;
        else if (type == 2) {
            return rectX >= point1.x && rectX < point6.x && rectY > point2.y;
        } else {
            return rectX >= point1.x && rectX < point9.x && rectY > point2.y;
        }
    }

    // check if x coordinate of rectangle is inside triangle
    public boolean xIsInArea(int rectX) {
        if (type == 1)
            return rectX >= point1.x && rectX < point3.x;
        else if (type == 2) {
            return rectX >= point1.x && rectX < point6.x;
        } else {
            return rectX >= point1.x && rectX < point9.x;
        }
    }

    // compute cross product to determine on which side of line(from a to b) the point is -> to check for collision
    public static int crossProduct(Point a, Point b, int rectX, int rectY){
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

package com.example.yanfa.pengcit1;

/**
 * Created by yanfa on 9/13/2015.
 */
public class Point {
    private int x;
    private int y;

    public Point(){
        x = 0;
        y = 0;
    }

    public Point(Point p){
        x = p.getX();
        y = p.getY();
    }

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}

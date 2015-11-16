/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.yanfa.pengcit1;
/**
 *
 * @author ahmadshahab
 */
public class Point {
    
    private int x;
    private int y;
    
    public Point(){}
    
    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public Point(Point P){
        this.x = P.x;
        this.y = P.y;
    }
        
    public void setPoint(Point P){
        this.x = P.getX();
        this.y = P.getY();
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public void setX(int x){
        this.x = x;
    }
    
    public void setY(int y){
        this.y = y;
    }
}

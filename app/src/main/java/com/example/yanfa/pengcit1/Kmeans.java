/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.yanfa.pengcit1;

import java.util.ArrayList;

/**
 *
 * @author ahmadshahab
 */
public class Kmeans {

    /* Attribute */
    private Point[] pixels;
    private ArrayList<ArrayList<Point>> categorizedPixels;
    private int numberCategory;
    private final int NUM_ITERATION;
    
    /* Initialize */
    /* parameter image: all of the black pixels */
    public Kmeans(Point[] image, int numCategory, int numIteration){
        pixels = new Point[image.length];
        
        for(int i = 0; i < image.length; i++){
            pixels[i] = new Point();
            pixels[i].setX(image[i].getX());
            pixels[i].setY(image[i].getY());
        }
        
        numberCategory = numCategory;
        categorizedPixels = new ArrayList<>();
        for(int i = 0; i < numberCategory; i++){
            ArrayList<Point> temp = new ArrayList<>();
            categorizedPixels.add(temp);
        }
        
        NUM_ITERATION = numIteration;
    }
    
    /**
     * @return the pixels
     */
    public Point[] getPixels() {
        return pixels;
    }

    /**
     * @param pixels the pixels to set
     */
    public void setPixels(Point[] pixels) {
        this.pixels = pixels;
    }

    /**
     * @return the numberCategory
     */
    public int getNumberCategory() {
        return numberCategory;
    }

    /**
     * @param numCategory 
     */
    public void setNumberCategory(int numCategory) {
        this.numberCategory = numCategory;
    }
    
    /**
     * @return the categorizedPixels
     */
    public ArrayList<ArrayList<Point>> getCategorizedPixels() {
        return categorizedPixels;
    }
    
    public double euclideanDistance(Point a, Point b){
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
        
    public void categorizePixels(){
        Point[] centroids = new Point[numberCategory];
        
        /* Intialize the categorizedPixel first */
        int x_min = pixels[0].getX(), x_max = pixels[0].getX(), y_min = pixels[0].getY(), y_max = pixels[0].getY();
        for(Point point: pixels){
            if(x_min > point.getX()) x_min = point.getX();
            if(y_min > point.getY()) y_min = point.getY();
            if(x_max < point.getX()) x_max = point.getX();
            if(y_max < point.getY()) y_max = point.getY();
        }
        
        /* Assume the centroid is distributed normally followed the pattern of human face */
        for(int i = 0; i < 4; i++){
            centroids[i] = new Point();
            centroids[i].setX((x_max - x_min) * (i + 1) / numberCategory);
            centroids[i].setY((y_max - y_min) * (i + 1) / numberCategory);
        }
        centroids[0].setX((x_max - x_min)/2 - (x_max - x_min)/4);
        centroids[0].setY((y_max - y_min)/2 - (y_max - y_min)/4);

        centroids[1].setX((x_max - x_min)/2 + (x_max - x_min)/4);
        centroids[1].setY((y_max - y_min)/2 - (y_max - y_min)/4);

        centroids[2].setX((x_max - x_min)/2);
        centroids[2].setY((y_max - y_min)/2 + ((x_max - x_min)/8));

        centroids[3].setX((x_max - x_min) / 2);
        centroids[3].setY((y_max - y_min) / 2 + ((x_max - x_min)/4));
        
        /* based on the position of point, find the pixels with shortest distance between them */
        for(int i = 0; i < numberCategory; i++){
            double shortestDistance = euclideanDistance(pixels[0], centroids[i]);
            Point tempCentroids = new Point(centroids[i]);
            
            for(int j = 1; j < pixels.length; j++){
                double temp = euclideanDistance(pixels[j], centroids[i]);
                if(shortestDistance > temp && temp>20){
                    shortestDistance = temp;
                    tempCentroids = pixels[j];
                }
            }
            centroids[i].setPoint(tempCentroids);
        }
        /* centroids already intialized correctly! */
        
        System.out.println("Centroid:");
        for(int i = 0; i < numberCategory; i++){
            System.out.printf("%d , %d\n", centroids[i].getX(),centroids[i].getY());
        }
        
        /* Start the iteration */
        int iterator = 0;
        while(iterator < NUM_ITERATION){
            // create a temp to copy old data
            ArrayList<ArrayList<Point>> tempPixels = new ArrayList<>();
            for(int i = 0; i < numberCategory; i++)
            {
                ArrayList<Point> temp = new ArrayList<>();
                tempPixels.add(temp);
            }
            for(int i = 0; i < tempPixels.size(); i++){
                for(Point p: tempPixels.get(i)){
                    tempPixels.get(i).set(i, p);
                }
            }
            // Clear everything to begin a new cluster
            categorizedPixels.clear();
            for(int i = 0; i < numberCategory; i++){
                ArrayList<Point> temp = new ArrayList<>();
                categorizedPixels.add(temp);
            }
            
            for(int indexPoint = 0; indexPoint < pixels.length; indexPoint++){
                int centroidIndex = 0;
                double tempDistance = euclideanDistance(pixels[indexPoint], centroids[0]);
                for(int numCentroid = 1; numCentroid < numberCategory; numCentroid++){
                    double temp = euclideanDistance(pixels[indexPoint], centroids[numCentroid]);
                    if(tempDistance > temp){
                        centroidIndex = numCentroid;
                        tempDistance = temp;
                    }
                }
                getCategorizedPixels().get(centroidIndex).add(pixels[indexPoint]);
            }
            iterator++;

            /* Find new centroid again for next iteration*/
            for(int i = 0; i < numberCategory; i++){
                int x_average = 0, y_average = 0;
                for(Point pixel: categorizedPixels.get(i)){
                    x_average += pixel.getX();
                    y_average += pixel.getY();
                }
                if(categorizedPixels.get(i).size() < 0) {
                    centroids[i].setX(x_average / categorizedPixels.get(i).size());
                    centroids[i].setY(y_average / categorizedPixels.get(i).size());
                }
            }
            
            System.out.println("Centroid:");
            for(int i = 0; i < numberCategory; i++){
                System.out.printf("%d , %d\n", centroids[i].getX(),centroids[i].getY());
            }
        }
        
        /* HASIL AKHIR PEMBAGIAN SESUAI DENGAN KATEGORI */
        for (ArrayList<Point> categorizedPixel : getCategorizedPixels()) {
            System.out.println("size: " + categorizedPixel.size());
            for (Point categorizedPixel1 : categorizedPixel) {
                System.out.print(categorizedPixel1.getX() + "-" + categorizedPixel1.getY() + " ");
            }
            System.out.println();
        }
    }
    
//    public static void main(String[] args){
//        Point[] image = new Point[6];
//        for(int i = 0; i < image.length; i++){
//            image[i] = new Point();
//            image[i].setX(i);
//            image[i].setY(i);
//        }
//
//        /* NUMBER OF ITERATION COULD BE CHANGED MANUALLY IN THE THIRD PARAMETER */
//        Kmeans classifier = new Kmeans(image, 4, 10);
//        classifier.categorizePixels();
//    }
}   

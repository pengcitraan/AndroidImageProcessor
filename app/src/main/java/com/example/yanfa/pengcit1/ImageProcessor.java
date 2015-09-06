package com.example.yanfa.pengcit1;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanfa on 9/6/2015.
 */
public class ImageProcessor {

    private static int[][] convertTo2DWithoutUsingGetRGB(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] result = new int[height][width];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //result[i][j] = image.getPixel(i,j);
            }
        }
        return result;
    }

    public static int countTotalColor(Bitmap image){

        System.out.println("size image" + image.getWidth() + "," + image.getHeight());
        // Convert to pixels
        Map<Integer, Integer> Counter;
        Counter = new HashMap<>();

        for(int i = 0; i < image.getWidth(); i++){
            for(int j = 0; j < image.getHeight(); j++){
                Counter.put(image.getPixel(i,j), 0);
            }
        }
        return Counter.size();
    }

    public static int[][][] countColor(Bitmap image){

        int[][][] result = new int[256][256][256];
        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                for(int k = 0; k < 256; k++){
                    result[i][j][k] = 0;
                }
            }
        }

        for(int i = 0; i < image.getWidth(); i++){
            for(int j = 0; j < image.getHeight(); j++){
                int p = image.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                result[R][G][B]++;
            }
        }
        return result;
    }
}

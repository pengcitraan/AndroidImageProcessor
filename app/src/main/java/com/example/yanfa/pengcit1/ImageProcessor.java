package com.example.yanfa.pengcit1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

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

    public static Bitmap toGrayscale(Bitmap bmpOriginal){
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap histogramEqualization(Bitmap bitmap, int adjustRed, int adjustCuf){
        int redFreq[] = new int[256];
        int redCuF[] = new int[256];
        int feq[] = new int[256];
        int cuFeq[] = new int[256];
        int map[] = new int[256];

        Bitmap greyscaleBitmap = toGrayscale(bitmap);

        int totalPixel = greyscaleBitmap.getHeight() + greyscaleBitmap.getWidth();

        int width, height;
        height = greyscaleBitmap.getHeight();
        width = greyscaleBitmap.getWidth();

        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap hisEqBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                redFreq[R]++;
            }
        }

        redCuF[0] = redFreq[0] + adjustRed;
        for(int i = 1; i < 256; i++){
            redCuF[i] = redCuF[i-1] + redFreq[i];
        }

        for(int i = 0; i < 256; i++){
            feq[i] = totalPixel / 256;
        }

        cuFeq[0] = feq[0] + adjustCuf;
        for(int i = 1; i < 256; i++){
            cuFeq[i] = cuFeq[i-1] + feq[i];
        }

        for(int i = 0; i < 256; i++){
            int counter = 1;
            int smallestDiff = Math.abs((redCuF[i] - cuFeq[0]));
            while(counter < 256){
                int tempDiff = Math.abs((redCuF[i] - cuFeq[counter]));
                if(smallestDiff > tempDiff){
                    smallestDiff = tempDiff;
                    counter++;
                }
                if(smallestDiff <= tempDiff) {
                    counter--;
                    break;
                }
            }
            map[i] = counter;
        }

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                tempBitmap.setPixel(i,j, Color.rgb(map[R], G, B));
            }
        }

        hisEqBitmap = toGrayscale(tempBitmap);

        return hisEqBitmap;
    }
}

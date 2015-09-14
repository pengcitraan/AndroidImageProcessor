package com.example.yanfa.pengcit1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        int feq[] = new int[256];
        int cuFeq[] = new int[256];

        int redFreq[] = new int[256];
        int redCuF[] = new int[256];
        int mapRed[] = new int[256];

        int blueFreq[] = new int[256];
        int blueCuF[] = new int[256];
        int mapBlue[] = new int[256];

        int greenFreq[] = new int[256];
        int greenCuF[] = new int[256];
        int mapGreen[] = new int[256];

        Bitmap greyscaleBitmap = bitmap;

        int totalPixel = greyscaleBitmap.getHeight() + greyscaleBitmap.getWidth();

        int width, height;
        height = greyscaleBitmap.getHeight();
        width = greyscaleBitmap.getWidth();

        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap hisEqBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int i = 0; i < 256; i++){
            feq[i] = totalPixel / 256;
        }

        cuFeq[0] = feq[0];
        for(int i = 1; i < 256; i++){
            cuFeq[i] = cuFeq[i-1] + feq[i];
        }

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                redFreq[R]++;
            }
        }

        redCuF[0] = redFreq[0];
        for(int i = 1; i < 256; i++){
            redCuF[i] = redCuF[i-1] + redFreq[i];
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
                else {
                    counter--;
                    System.out.println("counter : " + counter);
                    System.out.println("redcuf : " + redCuF[i]);
                    System.out.println("cuFeq : " + cuFeq[counter]);
                    System.out.println("smallestDiff : " + smallestDiff);
                    System.out.println("tempDiff : " + tempDiff);
                    break;
                }
            }
            mapRed[i] = counter;
        }
        /*
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int B = (p & 0x0000ff) >> 0;
                blueFreq[B]++;
            }
        }

        blueCuF[0] = blueFreq[0];
        for(int i = 1; i < 256; i++){
            blueCuF[i] = blueCuF[i-1] + blueFreq[i];
        }

        for(int i = 0; i < 256; i++){
            int counter = 1;
            int smallestDiff = Math.abs((blueCuF[i] - cuFeq[0]));
            while(counter < 256){
                int tempDiff = Math.abs((blueCuF[i] - cuFeq[counter]));
                if(smallestDiff > tempDiff){
                    smallestDiff = tempDiff;
                    counter++;
                }
                else if (smallestDiff <= tempDiff) {
                    counter--;
                    break;
                }
            }
            mapBlue[i] = counter;
        }

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int G = (p & 0x00ff00) >> 8;
                greenFreq[G]++;
            }
        }

        greenCuF[0] = greenFreq[0];
        for(int i = 1; i < 256; i++){
            greenCuF[i] = greenCuF[i-1] + greenFreq[i];
        }

        for(int i = 0; i < 256; i++){
            int counter = 1;
            int smallestDiff = Math.abs((greenCuF[i] - cuFeq[0]));
            while(counter < 256){
                int tempDiff = Math.abs((greenCuF[i] - cuFeq[counter]));
                if(smallestDiff > tempDiff){
                    smallestDiff = tempDiff;
                    counter++;
                }
                else if(smallestDiff <= tempDiff) {
                    counter--;
                    break;
                }
            }
            mapGreen[i] = counter;
        }*/

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                tempBitmap.setPixel(i, j, Color.rgb(mapRed[R], G, B));
                //System.out.println("R" + mapRed[R] + "G" + mapGreen[G] + "B" +mapBlue[B]);
            }
        }

        hisEqBitmap = toGrayscale(tempBitmap);

        return tempBitmap;
    }

    public List<String> getChaineCodes(Bitmap image){
        List<String> chaineCodes = new ArrayList<String>();
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();

        for(int i = 0; i < imageWidth; i++){
            for(int j = 0; j < imageHeight; j++){
                int p = image.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                if(R == 0 && G == 0 && B == 0){
                    String tempString = getObject(image, new Point(i,j), new Point(i,j), true, getDirection(2));
                    chaineCodes.add(tempString);
                    break;
                }
            }
        }
        return chaineCodes;
    }

    public int classifyChainCodes(Bitmap image, int indexChainCode){
        String chainCode = getChaineCodes(image).get(indexChainCode);
        List<String> chainCodeSplit = new ArrayList<String>();
        chainCodeSplit = chainSplit(chainCode);
        return 0;
    }

    List<String> chainCodeSplit = new ArrayList<String>();

    public List<String> chainSplit(String chainCode){
        if(chainCode.isEmpty()){
            return chainCodeSplit;
        }
        else{
            for (int i = 0; i < chainCode.length(); i++) {
                if (chainCode.charAt(i - 1) != chainCode.charAt(i) || i == chainCode.length()) {
                    String tempString = chainCode.split(String.valueOf(chainCode.charAt(i-1)), 2)[0];
                    chainCodeSplit.add(tempString);
                    String contString = chainCode.split(String.valueOf(chainCode.charAt(i-1)), 2)[1];
                    chainCodeSplit.addAll(chainSplit(contString));
                }
            }
        }
        return chainCodeSplit;
    }

    public String getObject(Bitmap image, Point curPos, Point initPos, Boolean start, int lastPos){
        if(curPos.getX() == initPos.getX() && curPos.getY() == initPos.getY() && !start){
            return "";
        }
        else{
            if(getNextObejctPixel(image,curPos,lastPos) == 0){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX(), curPos.getY() - 1), initPos, false, getDirection(0));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 1){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() + 1, curPos.getY() - 1), initPos, false, getDirection(1));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 2){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() + 1, curPos.getY()), initPos, false, getDirection(2));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 3){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() + 1, curPos.getY() + 1), initPos, false, getDirection(3));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 4){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX(), curPos.getY() + 1), initPos, false, getDirection(4));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 5){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() - 1, curPos.getY() + 1), initPos, false, getDirection(5));
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 6){
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() - 1, curPos.getY()), initPos, false, getDirection(6));
            }
            else{
                return "" + getNextObejctPixel(image, curPos, lastPos) + ""
                        + getObject(image, new Point(curPos.getX() - 1, curPos.getY() - 1), initPos, false, getDirection(7));
            }
        }
    }

    private int getNextObejctPixel(Bitmap image, Point pos, int lastPos){
        int initMove = (lastPos + 2) % 8;
        int height = image.getHeight();
        int width = image.getWidth();
        for(int i = initMove; i < initMove + 8; i++){
            if((i%8) == 0 && pos.getY() != 0
                    && isBlack(image, new Point(pos.getX(), pos.getY() - 1))){
                return 0;
            }
            else if((i%8) == 1 && pos.getY() != 0 && pos.getX() != (width - 1)
                    && isBlack(image, new Point(pos.getX() + 1, pos.getY() - 1))){
                return 1;
            }
            else if((i%8) == 2 && pos.getX() != (width - 1)
                    && isBlack(image, new Point(pos.getX() + 1, pos.getY()))){
                return 2;
            }
            else if((i%8) == 3 && pos.getY() != (height - 1) && pos.getX() != (width - 1)
                    && isBlack(image, new Point(pos.getX() + 1, pos.getY() + 1))){
                return 3;
            }
            else if((i%8) == 4 && pos.getY() != (height - 1)
                    && isBlack(image, new Point(pos.getX(), pos.getY() + 1))){
                return 4;
            }
            else if((i%8) == 5 && pos.getY() != (height - 1) && pos.getX() != 0
                    && isBlack(image, new Point(pos.getX() - 1, pos.getY() + 1))){
                return 5;
            }
            else if((i%8) == 6 && pos.getX() != 0
                    && isBlack(image, new Point(pos.getX() - 1, pos.getY()))){
                return 6;
            }
            else if((i%8) == 7 && pos.getY() != 0 && pos.getX() != 0
                    && isBlack(image, new Point(pos.getX() - 1, pos.getY() - 1))){
                return 7;
            }
        }
        return 8;
    }

    private int getDirection(int move){
        switch (move){
            case 0 : return 4;
            case 1 : return 5;
            case 2 : return 6;
            case 3 : return 7;
            case 4 : return 0;
            case 5 : return 1;
            case 6 : return 2;
            case 7 : return 3;
            default : return 2;
        }
    }

    private Boolean isBlack(Bitmap image, Point pos){
        int p = image.getPixel(pos.getX(),pos.getY());
        int R = (p & 0xff0000) >> 16;
        int G = (p & 0x00ff00) >> 8;
        int B = (p & 0x0000ff) >> 0;
        if(R == 0 && G == 0 && B == 0){
            return true;
        }
        else{
            return false;
        }
    }

}

package com.example.yanfa.pengcit1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by yanfa on 9/6/2015.
 */
public class ImageProcessor {

    private Bitmap current_image;
    private boolean[][] mark;

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

    public static Bitmap toGrayscale(Bitmap bmpOriginal, float threshold){
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

        threshold = ImageProcessor.otsuThresholder(toGrayscale(bmpOriginal))/new Float(256);
        System.out.println("Threshold: " + threshold);
        for(int i = 0; i < bmpGrayscale.getWidth(); i++){
            for(int j = 0; j < bmpGrayscale.getHeight(); j++){
                if(bmpGrayscale.getPixel(i, j) < (Color.BLACK)*threshold){
                    bmpGrayscale.setPixel(i, j, Color.WHITE);
                }
                else{
                    bmpGrayscale.setPixel(i, j, Color.BLACK);
                }
            }
        }

        return bmpGrayscale;
    }

    /*
    * @param Grayscaled image
    * */
    public static int otsuThresholder(Bitmap bmpGrayscale){
        int histogram[] = new int[256];
        for (int i = 0; i < bmpGrayscale.getWidth(); i++){
            for (int j = 0; j < bmpGrayscale.getHeight(); j++){
                int p = bmpGrayscale.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                histogram[R] += 1;
            }
        }

        int total = bmpGrayscale.getWidth() * bmpGrayscale.getHeight();

        float sum = 0;
        for(int t = 0; t < 256; t++) sum += t * histogram[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int t=0 ; t<256 ; t++) {
            wB += histogram[t];               // Weight Background
            if (wB == 0) continue;

            wF = total - wB;                 // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histogram[t]);

            float mB = sumB / wB;            // Mean Background
            float mF = (sum - sumB) / wF;    // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        return  threshold;
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

        // Supaya bisa dihapus nantinya pake floodfill
        current_image = image;
        mark = new boolean[imageWidth][imageHeight];
        for(int i = 0; i < imageWidth; i++){
            for(int j = 0; j < imageHeight; j++){
                int p = image.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                if(R == 0 && G == 0 && B == 0){
                    System.out.println("Nilai p: " + p);
                    //String tempString = getObject(image, new Point(i,j), new Point(i,j), true, getDirection(2));
                    chaineCodes.add("111");
                    //System.out.println("Indeks: " + i + ',' + j);
                    // removeObject(i, j);
                }
            }
        }
        return chaineCodes;
    }


    public String getNumber (String pattern){
        String[] numbers = new String[10];
        numbers[1] = new String("22444444444444444444446600000000000000000000");
        numbers[2] = new String("10122222223344444444556556544444432222224446666666666600000000000111211100007654444666600000");
        numbers[3] = new String("1122222234444444446532444444444444566666677000000002224444444321100000007766002221000007765444466600000");
        numbers[4] = new String ("111211112111224444444443224665444600076666666666660");
        numbers[5] = new String ("22222222224444444666000076654444443222234344444444454566666670700000022244444432210000000007666666000000000000");
        numbers[6] = new String ("1122222234444466000766544444443122234344444444454566666677000000000000000000000");
        numbers[7] = new String ("22222222445444454444544445444454466601000010001000010000107666600");
        numbers[8] = new String ("112222223344444445653243444444444556666667700000000001217670000000");
        numbers[9] = new String ("112222223434444444444444444444545666666707000222444322100000007566660700000000000");
        numbers[0] = new String ("11222222343444444444444444444454566666677000000000000000000000");

        int minVal = 10000;
        int minIdx = 10;
        for (int i = 0; i < numbers.length; i++){
            int val = getDistance(pattern, numbers[i]);
            int idx = i;
            if (minVal >  val){
                minVal = val;
                minIdx = i;
            }
        }

        String result = new String(""+ minIdx);
        return result;
    }

    public int getDistance (String pattern, String number){
        int factor;
        String sNumber = new String("");
        int value;

        if (pattern.length() > number.length()){
            factor  = pattern.length() / number.length();
        } else {
            factor = 1;
        }
//        System.out.println("Factor " + factor);
        for (int i = 0; i < number.length(); i++){
            String temp = new String (""+ number.charAt(i));
//            System.out.println("Temp: " + temp);
            for (int j = 0; j < factor ; j++){
                sNumber += temp;
//                System.out.println("Stretch Number: " + sNumber);
            }

        }

        System.out.println("Pattern: " + pattern);
        System.out.println("Pattern length: " + pattern.length());
        System.out.println("Number: " + number);
        System.out.println("Number length: " + number.length());
        System.out.println("Stretch Number: " + sNumber);
        System.out.println("Stretch Number length: " + sNumber.length());


        value = editDistance(sNumber, pattern);
        System.out.println("Difference Value: " + value + "\n\n");
        return  value;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public String detectPattern(String pattern){
        return getNumber(pattern);
    }

    public void removeObject(int i, int j){
        Point node = new Point(i, j);
        Queue<Point> Q = new ArrayDeque<Point>();
        Q.add(node);
        while (Q.size() > 0){
            Point n = Q.poll();
            i = n.getX();
            j = n.getY();
            if(i < 0 || i >= current_image.getWidth() || j < 0 || j >= current_image.getHeight() || mark[i][j]){
                // do nothing
                // System.out.println("Do nothing on " + i + ',' + j);
            }
            else {
                if (isBlack(current_image, n)) {
                    current_image.setPixel(i, j, Color.WHITE);
                    mark[i][j] = true;
                    Q.add(new Point(i + 1, j));
                    Q.add(new Point(i - 1, j));
                    Q.add(new Point(i, j + 1));
                    Q.add(new Point(i, j - 1));
                    Q.add(new Point(i + 1, j + 1));
                    Q.add(new Point(i - 1, j + 1));
                    Q.add(new Point(i - 1, j - 1));
                    Q.add(new Point(i + 1, j - 1));
                }
            }
        }
    }

    public Bitmap getCurrent_image(){
        return current_image;
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

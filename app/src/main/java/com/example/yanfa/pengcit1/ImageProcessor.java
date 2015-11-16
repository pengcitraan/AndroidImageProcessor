package com.example.yanfa.pengcit1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import java.io.FileNotFoundException;
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
    private int MAX_CHAIN_CODE = 6; // max char detected for turn or not in chain code
    private int MIN_DEGREE_FOR_TURN = 180; // min degree to estimate as a corner
    private int KANAN = 1;
    private int KIRI = 0;


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

    public static Bitmap toGrayscale(Bitmap bmpOriginal, float threshold) {
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

        threshold += ImageProcessor.otsuThresholder(toGrayscale(bmpOriginal)) / new Float(256) ;//- new Float(0.2)
        System.out.println("Threshold: " + threshold);
        for (int i = 0; i < bmpGrayscale.getWidth(); i++) {
            for (int j = 0; j < bmpGrayscale.getHeight(); j++) {
                if (bmpGrayscale.getPixel(i, j) < (Color.BLACK) * threshold) {
                    bmpGrayscale.setPixel(i, j, Color.WHITE);
                } else {
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

        int grayFreq[] = new int[256];
        double grayProbFreq[] = new double[256];
        double grayCuProbFreq[] = new double[256];
        int grayValueNew[] = new int[256];

        Bitmap greyscaleBitmap = toGrayscale(bitmap);

        int totalPixel = greyscaleBitmap.getHeight() * greyscaleBitmap.getWidth();

        int width, height;
        height = greyscaleBitmap.getHeight();
        width = greyscaleBitmap.getWidth();

        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                double GRAY = (0.299 * R)  + (0.587 * G) + (0.114 * B);
                int grayValueOld = (int) GRAY;
//                System.out.println("Gray val : " + gray);
                grayFreq[grayValueOld]++;
            }
        }
//        System.out.println("Total Pixel : " + width*height);
//        int sumVal = 0;
//        for(int i = 0; i < 256; i++){
//            sumVal = sumVal + grayFreq[i];
//        }
//        System.out.println("Total Value : " + sumVal);
        int GrayLevel = 255;

        for(int i = 0; i < 256; i++){
            grayProbFreq[i] = (double)((grayFreq[i] + adjustCuf))  / totalPixel;
//            System.out.println("Gray Value " + i + " Freq : "  + grayFreq[i]);
//            System.out.println("Gray Value " + i + " Prob Freq : "  + grayProbFreq[i]);
        }

        grayCuProbFreq[0] = grayProbFreq[0];
        for(int i = 1; i < 256; i++){
            grayCuProbFreq[i] = grayCuProbFreq[i-1] + grayProbFreq[i];
//            System.out.println("Gray Value " + i + " Cum Prob Freq : "  + grayCuProbFreq[i]);
        }


        for(int i = 0; i < 256; i++){
            int newValue = (int) (grayCuProbFreq[i] * GrayLevel);
            if (newValue > 255){
                grayValueNew[i] = 255;
            } else {
                grayValueNew[i] = newValue;
            }
//            grayValueNew[i] = (int) ((grayCuProbFreq[i] * GrayLevel) % 255);
//            System.out.println("Gray Value Old:" + i + "\n Gray Value New : "  + grayValueNew[i]);
        }

        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                double GRAY = (0.299 * R) + (0.587 * G) + (0.114 * B);
                int grayValueOld = (int) GRAY;
                tempBitmap.setPixel(i, j, Color.rgb(grayValueNew[grayValueOld], grayValueNew[grayValueOld], grayValueNew[grayValueOld]));
            }
        }

        return tempBitmap;
    }

    public static Bitmap histogramEqualization(Bitmap bitmap, int adjust){
        System.out.println("masuk coy");
        int grayFreq[] = new int[256];
        double grayProbFreq[] = new double[256];
        double grayCuProbFreq[] = new double[256];
        int grayValueNew;

        Bitmap greyscaleBitmap = toGrayscale(bitmap);

        int totalPixel = greyscaleBitmap.getHeight() * greyscaleBitmap.getWidth();

        int width, height;
        height = greyscaleBitmap.getHeight();
        width = greyscaleBitmap.getWidth();

        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                double GRAY = (0.299 * R)  + (0.587 * G) + (0.114 * B);
                int grayValueOld = (int) GRAY;
//                System.out.println("Gray val : " + gray);
                grayFreq[grayValueOld]++;
            }
        }
        double lowerLimit = 0;
        double upperLimit = 255;
        double smallestVal = 0;
        double biggestVal = 255;

        int sumFreq = 0;
        for (int i = 0; i < 256; i++){
            sumFreq = sumFreq + grayFreq[i];
            if (sumFreq > (double)totalPixel * (0.05 + (adjust * 0.01))){
                smallestVal = i-1;
                break;
            }
        }

        sumFreq = totalPixel;
        for (int i = 255; i >= 0; i--){
            sumFreq = sumFreq - grayFreq[i];
            if (sumFreq < (double)totalPixel * (0.95 - (adjust * 0.01))){
                biggestVal = i+1;
                break;
            }
        }
        System.out.println("smallestVal: " + smallestVal +  "\nbiggestVal: " + biggestVal);

        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int p = greyscaleBitmap.getPixel(i, j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                double GRAY = (0.299 * R) + (0.587 * G) + (0.114 * B);
                int grayValueOld = (int) GRAY;
                grayValueNew =(int) (((double)grayValueOld - smallestVal) * ((upperLimit - lowerLimit) / (biggestVal - smallestVal)) + lowerLimit);
                if (grayValueNew > 255){
                    grayValueNew = 255;
                } else if (grayValueNew < 0){
                    grayValueNew = 0;
                }
                tempBitmap.setPixel(i, j, Color.rgb(grayValueNew, grayValueNew, grayValueNew));
            }
        }

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
                int p = current_image.getPixel(i,j);
                int R = (p & 0xff0000) >> 16;
                int G = (p & 0x00ff00) >> 8;
                int B = (p & 0x0000ff) >> 0;
                if(R == 0 && G == 0 && B == 0){
                    String tempString = getObject2(current_image, new Point(i, j), getDirection(2));
                    chaineCodes.add(tempString);
                    removeObject(i, j);
                }
            }
        }
        current_image = image;
        return chaineCodes;
    }

    public String getNumberChainCode (String pattern){
        String[] numbers = new String[30];
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
        numbers[11] = new String("21111111101110110110101211222323356555454545454544545444544544454444454444444344667676700000100001000100100010010100755555555556600");
        numbers[12] = new String("100122222222111211111111111101111011010010000007766666566566556556554543446777707001112112112121212212212223243321224344444444445454554555555555555565555556532223222223222322322322323344445467076766766676667666667666666666665545667700000");
        numbers[13] = new String("223322322222122122121221211212111211111111101000777676766766666666656670000002112212112121111111100776666566565565555545707700102112121122121221222333333434445555565565565565532322223223223233234454434444545455555655556555656556565656566656666667677777700");
        numbers[14] = new String ("121111111111011110110101100707712111011011011223234454545454545455455455455455545555545554322222222222222222221010010001001001001010010122225455445454445445445444454322211124443444566666666545444544445444434667677000100001000107666666666666666666666654667700000");
        numbers[15] = new String ("223232222121211121112111111111011111011010110100100100007776666656566565565655555670707070100101010010010010007070012222222322222223222222222211134344444456667666667666666766666665454545445454454454312121212121221221222232343232333444454454544545545455455545555545555555655556555656565666767777700");
        numbers[16] = new String ("1000000100010001001001001010100101101010111011101111121212223233233467665666556554555545554545545454545445454454454445444445444444444443333222221211111111010110101010100100100007766566566565655656600211211212121212223233232344444544454454545454554554555555556566666676666766767770000000000");
        numbers[17] = new String ("101011010101101010110101101011010110101101011010110101101011767666666667666566666565555455600010001001001122222222322222222222322222112223444444565545545545454554545454545545454545454545454545454545454544545445445444467770707000");
        numbers[18] = new String ("10110111111112111121111211777770707000000100100110111111112112223233346766656555555455454544544444343332211111111110111101101000077700223323334344445455555555565555565555433323332333434344444544554555555565565656565666566666676707776777770000");
        numbers[19] = new String ("222232212211211111101110110110101101010101010100101001001000100001000000070776766665655555555555455454545454454445434322221212121212112124555555555565656667676767660700000100100101010110101110111111112121222231223223232323434444445444454445445445445445454454545454545454554545545545545555555556656667676777600");
        numbers[10] = new String ("1000010010010010101010110110111122232232222122222232434444444444454444454445444544544545454545555565666666676667676777670700000000");
        numbers[21] = new String("21111111101110110110101211222323356555454545454544545444544544454444454444444344667676700000100001000100100010010100755555555556600");
        numbers[22] = new String("100122222222111211111111111101111011010010000007766666566566556556554543446777707001112112112121212212212223243321224344444444445454554555555555555565555556532223222223222322322322323344445467076766766676667666667666666666665545667700000");
        numbers[23] = new String("223322322222122122121221211212111211111111101000777676766766666666656670000002112212112121111111100776666566565565555545707700102112121122121221222333333434445555565565565565532322223223223233234454434444545455555655556555656556565656566656666667677777700");
        numbers[24] = new String ("121111111111011110110101100707712111011011011223234454545454545455455455455455545555545554322222222222222222221010010001001001001010010122225455445454445445445444454322211124443444566666666545444544445444434667677000100001000107666666666666666666666654667700000");
        numbers[25] = new String ("223232222121211121112111111111011111011010110100100100007776666656566565565655555670707070100101010010010010007070012222222322222223222222222211134344444456667666667666666766666665454545445454454454312121212121221221222232343232333444454454544545545455455545555545555555655556555656565666767777700");
        numbers[26] = new String ("1000000100010001001001001010100101101010111011101111121212223233233467665666556554555545554545545454545445454454454445444445444444444443333222221211111111010110101010100100100007766566566565655656600211211212121212223233232344444544454454545454554554555555556566666676666766767770000000000");
        numbers[27] = new String ("101011010101101010110101101011010110101101011010110101101011767666666667666566666565555455600010001001001122222222322222222222322222112223444444565545545545454554545454545545454545454545454545454545454544545445445444467770707000");
        numbers[28] = new String ("10110111111112111121111211777770707000000100100110111111112112223233346766656555555455454544544444343332211111111110111101101000077700223323334344445455555555565555565555433323332333434344444544554555555565565656565666566666676707776777770000");
        numbers[29] = new String ("222232212211211111101110110110101101010101010100101001001000100001000000070776766665655555555555455454545454454445434322221212121212112124555555555565656667676767660700000100100101010110101110111111112121222231223223232323434444445444454445445445445445454454545454545454554545545545545555555556656667676777600");
        numbers[20] = new String ("22222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222244444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444446666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666660000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        int minVal = 10000;
        int minIdx = 10;
        for (int i = 0; i < numbers.length; i++){
            int val = getDistance(pattern, numbers[i]);
            int idx = i;
            //System.out.println("Turn Code " + i + " : " + getTurnCode(numbers[i]));
            if (minVal >  val){
                minVal = val;
                minIdx = i;
            }
        }

        String result = new String(""+ minIdx % 10);
        return result;
    }

    public String getNumberTurnCode (String pattern){
        String[] numbers = new String[20];
        numbers[0] = new String ("1111");
        numbers[1] = new String("1111");
        numbers[2] = new String("11100111000111");
        numbers[3] = new String("11011110001000111");
        numbers[4] = new String ("101001");
        numbers[5] = new String ("1110001111100011");
        numbers[6] = new String ("111001111");
        numbers[7] = new String ("111011");
        numbers[8] = new String ("11011101");
        numbers[9] = new String ("1111100011");
        numbers[10] = new String ("1111");
        numbers[11] = new String("00100001101");
        numbers[12] = new String("100000000011010110011");
        numbers[13] = new String("000000000110000000110001001");
        numbers[14] = new String ("00000011000001100001100010011");
        numbers[15] = new String ("000000000001100101100000100001");
        numbers[16] = new String ("0010000000000000000010");
        numbers[17] = new String ("00000000000000110110000001");
        numbers[18] = new String ("01000000000010010");
        numbers[19] = new String ("0000000000000000010001001");


        int minVal = 10000;
        int minIdx = 10;
        for (int i = 0; i < numbers.length; i++){
//            System.out.println("Pattern: " + pattern);
            int val = getDistance(pattern, numbers[i]);
            int idx = i;
            //System.out.println("Turn Code " + i + " : " + getTurnCode(numbers[i]));
            //System.out.println("EditDistance for " + i + " : " + val);
            if (minVal >  val){
                minVal = val;
                minIdx = i;
            }
        }
        System.out.println("Pattern: " + pattern);
        System.out.println("Matched Pattern: " + numbers[minIdx]);
        System.out.println("Result: " + minIdx % 10);
        String result = new String(""+ minIdx % 10);
        return result;
    }

    public int getDistance (String pattern, String number){
        int factor;
        String sNumber = new String("");
        int value;

        if (pattern.length() > number.length()){
            factor  = pattern.length() / number.length();
            for (int i = 0; i < number.length(); i++){
                String temp = new String (""+ number.charAt(i));
                for (int j = 0; j < factor ; j++){
                    sNumber += temp;
                }
            }
            value = editDistance(sNumber, pattern);
        } else {
            factor = number.length() / pattern.length();
            for (int i = 0; i < pattern.length(); i++){
                String temp = new String (""+ pattern.charAt(i));
                for (int j = 0; j < factor ; j++){
                    sNumber += temp;
                }
            }
            value = editDistance(sNumber, number);
        }
//        System.out.println("Pattern: " + pattern);
//        System.out.println("Pattern length: " + pattern.length());
//        System.out.println("Number: " + number);
//        System.out.println("Number length: " + number.length());
//        System.out.println("Stretch: " + sNumber);
//        System.out.println("Stretch Number length: " + sNumber.length());


//        value = editDistance(sNumber, pattern);
//        System.out.println("Difference Value: " + value + "\n\n");

        return  value;
    }

    public static int editDistance(String a, String b){
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public String detectPattern(String pattern){
        return getNumberChainCode(pattern);
//        String patternTurn = getTurnCode(pattern);
//        if (patternTurn.length() > 0){
//            return getNumberTurnCode(patternTurn);
//        } else {
//            return "";
//        }
    }

    public void removeObject(int i, int j){
        Point node = new Point(i, j);
        Queue<Point> Q = new ArrayDeque<Point>();
        Q.add(node);
        current_image = current_image.copy(Bitmap.Config.ARGB_8888, true);
        while (Q.size() > 0){
            Point n = Q.poll();
            i = n.getX();
            j = n.getY();
            if(i < 0 || i >= current_image.getWidth() || j < 0 || j >= current_image.getHeight() || mark[i][j]){
                // do nothing
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

    public Bitmap removeObject(Bitmap bmpImage, int i, int j, boolean model){
        int color_get, color_set;
        if(model){
            color_get = Color.WHITE;
            color_set = Color.BLACK;
        }
        else{
            color_get = Color.BLACK;
            color_set = Color.WHITE;
        }

        Point node = new Point(i, j);
        Queue<Point> Q = new ArrayDeque<Point>();
        Q.add(node);
        bmpImage = bmpImage.copy(Bitmap.Config.ARGB_8888, true);
        mark = new boolean[bmpImage.getWidth()][bmpImage.getHeight()];
        while (Q.size() > 0){
            Point n = Q.poll();
            i = n.getX();
            j = n.getY();
            if(i < 0 || i >= bmpImage.getWidth() || j < 0 || j >= bmpImage.getHeight() || mark[i][j]){
                // do nothing
            }
            else {
                if (bmpImage.getPixel(i, j) == color_get) {
                    bmpImage.setPixel(i, j, color_set);
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

        return bmpImage;
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

    public String getObject2(Bitmap image, Point pos, int lastPos){
        String chaineCode = "";
        Point curPos = new Point(pos);
        Point initPoint = new Point(pos);

        chaineCode = chaineCode + ""+getNextObejctPixel(image,curPos,lastPos);
        if(getNextObejctPixel(image,curPos,lastPos) == 0){
            curPos.setY(curPos.getY() - 1);
            lastPos = getDirection(0);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 1){
            curPos.setX(curPos.getX() + 1);
            curPos.setY(curPos.getY() - 1);
            lastPos = getDirection(1);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 2){
            curPos.setX(curPos.getX() + 1);
            lastPos = getDirection(2);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 3){
            curPos.setX(curPos.getX() + 1);
            curPos.setY(curPos.getY() + 1);
            lastPos = getDirection(3);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 4){
            curPos.setY(curPos.getY() + 1);
            lastPos = getDirection(4);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 5){
            curPos.setX(curPos.getX() - 1);
            curPos.setY(curPos.getY() + 1);
            lastPos = getDirection(5);
        }
        else if(getNextObejctPixel(image,curPos,lastPos) == 6){
            curPos.setX(curPos.getX() - 1);
            lastPos = getDirection(6);
        }
        else{
            curPos.setX(curPos.getX() - 1);
            curPos.setY(curPos.getY() - 1);
            lastPos = getDirection(7);
        }

        while(initPoint.getX() != curPos.getX() || initPoint.getY() != curPos.getY()){

            chaineCode = chaineCode + ""+getNextObejctPixel(image,curPos,lastPos);
            if(getNextObejctPixel(image,curPos,lastPos) == 0){
                curPos.setY(curPos.getY() - 1);
                lastPos = getDirection(0);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 1){
                curPos.setX(curPos.getX() + 1);
                curPos.setY(curPos.getY() - 1);
                lastPos = getDirection(1);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 2){
                curPos.setX(curPos.getX() + 1);
                lastPos = getDirection(2);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 3){
                curPos.setX(curPos.getX() + 1);
                curPos.setY(curPos.getY() + 1);
                lastPos = getDirection(3);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 4){
                curPos.setY(curPos.getY() + 1);
                lastPos = getDirection(4);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 5){
                curPos.setX(curPos.getX() - 1);
                curPos.setY(curPos.getY() + 1);
                lastPos = getDirection(5);
            }
            else if(getNextObejctPixel(image,curPos,lastPos) == 6){
                curPos.setX(curPos.getX() - 1);
                lastPos = getDirection(6);
            }
            else{
                curPos.setX(curPos.getX() - 1);
                curPos.setY(curPos.getY() - 1);
                lastPos = getDirection(7);
            }
        }

        return chaineCode;
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
        if (pos.getX() > image.getWidth() || pos.getX() < 0 || pos.getY() > image.getHeight() || pos.getY() < 0){
            return false;
        }
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

    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public Bitmap removeNoise(Bitmap bmpOriginal){
        Bitmap copy = bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);
        boolean noTransition = false;
        int countTransition = 0;

        // Remove pixel with no transition
        for (int i = 0; i < copy.getHeight(); i++){
            int curr_pixel = copy.getPixel(0, i);
            for (int j = 1; j < copy.getWidth(); j++){
                int next_pixel = copy.getPixel(j, i);
                if (curr_pixel == next_pixel && curr_pixel == Color.BLACK){
                    countTransition += 1;
                }
                else{
                    countTransition = 0;
                }

                int threshold = 4;
                if (countTransition > threshold){
                    //System.out.println("Remove at: " + j + ',' + i);
                    copy = removeLine(j-threshold, i, copy);
                    countTransition = 0;
                }

                curr_pixel = next_pixel;
            }
        }

        return copy;
    }

    public Bitmap removeLine(int i, int j, Bitmap current_image){
        if(i < 0) i = 0;
        //System.out.println("value of i: " + i);
        while (current_image.getPixel(i, j) == Color.BLACK){
            current_image.setPixel(i, j, Color.WHITE);
            i += 1;
            if(i >= current_image.getWidth()) return current_image;
        }
        return current_image;
    }

    public String getTurnCode(String chainCode){
        String turnCode = "";
        int i = 0;
        while(i < chainCode.length()){
            String tempCC = "";
            for(int j = 0; j < MAX_CHAIN_CODE; j++){
                if(((i+j) % chainCode.length()) < ((i+j+1) % chainCode.length())){
                    tempCC = tempCC + chainCode.substring(((i+j) % chainCode.length()), ((i+j+1) % chainCode.length()));
                }
                else{
                    tempCC = tempCC + chainCode.substring(chainCode.length() - 2, chainCode.length() - 1);
                }
            }
            int tempTurn = getCorner(tempCC);
            if(tempTurn != -1){
                i += (MAX_CHAIN_CODE - 2);
                turnCode = turnCode + tempTurn;
            }
            else{
                i++;
            }
        }
        return turnCode;
    }

    private int getCorner(String chainCode){
        int corner = -1;
        if(chainCode.length() != MAX_CHAIN_CODE){
            return corner;
        }

        int tempDegree = 0;
        for(int j = 0; j < MAX_CHAIN_CODE/2; j++){
            int tempCurrCekCode = Integer.parseInt(chainCode.substring((j) % chainCode.length(), (j + 1) % chainCode.length()));
            //System.out.println("" + (chainCode.substring((MAX_CHAIN_CODE-1-j) % chainCode.length())+ "," + (MAX_CHAIN_CODE-j) % chainCode.length()));
            int tempCurrCekCode2;
            if(((MAX_CHAIN_CODE-1-j) % chainCode.length()) < ((MAX_CHAIN_CODE-j) % chainCode.length())){
                tempCurrCekCode2 = Integer.parseInt(chainCode.substring((MAX_CHAIN_CODE-1-j) % chainCode.length(),(MAX_CHAIN_CODE-j) % chainCode.length()));
            }
            else{
                tempCurrCekCode2 = Integer.parseInt(chainCode.substring(chainCode.length() - 2, chainCode.length() - 1));
            }
            tempDegree += getDegree(tempCurrCekCode, tempCurrCekCode2);
        }
        if(tempDegree >= MIN_DEGREE_FOR_TURN){
            corner = KANAN;
            //System.out.println("kanan : " + chainCode);
        }
        else if(tempDegree <= (-1*MIN_DEGREE_FOR_TURN)){
            corner = KIRI;
            //System.out.println("kiri : " + chainCode);
        }
        return corner;
    }

    private int getDegree(int currentCC, int nextCC){
        int degree = 45;
        int pathFromPivotCC = (nextCC - currentCC);
        //System.out.println("path from pivot" + pathFromPivotCC);
        int cekFromPivot = pathFromPivotCC % 8;
        //System.out.println("cek from pivot" + cekFromPivot);
        int totalDiff = 0;
        if(pathFromPivotCC < 0){
            pathFromPivotCC += 8;
        }
        //System.out.println("path from pivot" + pathFromPivotCC);
        if(pathFromPivotCC > 4){
            totalDiff = ((pathFromPivotCC) % 4);
            degree *= -1;
        }
        else{
            totalDiff = pathFromPivotCC % 4;
        }
        return degree*totalDiff;
    }

    /**
     *
     * @param bmpImage
     * @return List of extreme Point: 0-> Y_Top ; 1-> Y_Bottom; 2-> X_Left; 3->X_Right
     */
    public List<Integer> getExtremePoints(Bitmap bmpImage, int i_start, int j_start, boolean model){

        int colour_;
        if(model){
            colour_ = Color.WHITE;
        }
        else{
            colour_ = Color.BLACK;
        }

        int y_top = bmpImage.getHeight();
        int y_bottom = j_start;
        int x_left = bmpImage.getWidth();
        int x_right = i_start;

        int crop_width = getPointCrop(bmpImage, model);

        for (int i = i_start; i < crop_width; i++){
            for (int j = j_start; j < bmpImage.getHeight(); j++){
                if (bmpImage.getPixel(i, j) == colour_){
                    if(j > y_bottom){
                        y_bottom = j;
                    }
                    if(j < y_top){
                        y_top = j;
                    }

                    if(i > x_right){
                        x_right = i;
                    }
                    if(i < x_left){
                        x_left = i;
                    }
                }
            }
        }

        ArrayList<Integer> extremePoint = new ArrayList<>();
        extremePoint.add(0, y_top);
        extremePoint.add(1, y_bottom);
        extremePoint.add(2, x_left);
        extremePoint.add(3, x_right);

        return extremePoint;
    }

    public int getPointCrop(Bitmap bmpImage, boolean model){
        int transition;
        int prev_color;
        int current_color;

        int i_crop = 0;
        boolean start = false;
        for(int i = 0; i < bmpImage.getWidth(); i++){
            transition = 0;
            prev_color = bmpImage.getPixel(i, 0);
            for(int j = 1; j < bmpImage.getHeight(); j++){
                current_color = bmpImage.getPixel(i, j);
                if(prev_color != current_color){
                    transition += 1;
                }
                prev_color = current_color;

                if(model && prev_color == Color.WHITE){
                    start = true;
                }
                else if(!model && prev_color == Color.BLACK){
                    start = true;
                }
            }
            if(start) {
                if (transition > 0) {
                    i_crop = i;
                } else {
                    return i_crop;
                }
            }
        }
        return  i_crop;
    }

    /**
     *
     * @param bmpImage
     * @param model if true then grid will be used for Modelling and Color will be changed to White, else Black
     * @return 5x5 integer of grid detection. 1 if the grid is filled
     */
    public int[][] gridDetection(Bitmap bmpImage, boolean model){

        int colour_;
        if(model){
            colour_ = Color.WHITE;
        }
        else{
            colour_ = Color.BLACK;
        }

//      Find the width & height of each block
        List<Integer> extremeP = getExtremePoints(bmpImage, 1, 1, model);
        int height = extremeP.get(1) - extremeP.get(0);
        int width = extremeP.get(3) - extremeP.get(2);

        int GRID_NUMBER = 15;

        int[][] gridDetector = new int[GRID_NUMBER][GRID_NUMBER];
        for (int i = 0; i < GRID_NUMBER; i++){
            for (int j = 0; j < GRID_NUMBER; j++){
                gridDetector[i][j] = 0;
            }
        }

        int gridIterator_i = 0;
        int gridIterator_j = 0;
        int count_width = 0;
        int count_height = 0;
        for(int i = extremeP.get(2); i < extremeP.get(3); i++){
            if(count_width > width/(float)GRID_NUMBER){
                if(GRID_NUMBER-1 > gridIterator_i) {
                    gridIterator_i += 1;
                    count_width = 0;
                }
            }
            for(int j = extremeP.get(0); j < extremeP.get(1); j++){
                if(count_height > height/(float)GRID_NUMBER){
                    if(GRID_NUMBER-1 > gridIterator_j){
                        gridIterator_j += 1;
                        count_height = 0;
                    }
                }
                if(bmpImage.getPixel(i, j) == colour_){
                    gridDetector[gridIterator_j][gridIterator_i] = 1;
                }
                count_height += 1;
            }
            gridIterator_j = 0;
            count_width += 1;
        }

        System.out.println("Width Image: " + bmpImage.getWidth());
        System.out.println("Height Image: " + bmpImage.getHeight());
        for(int i = 0; i < GRID_NUMBER; i++){
            for(int j = 0; j < GRID_NUMBER; j++){
                System.out.print(gridDetector[i][j] + " ");
            }
            System.out.println();
        }

        return gridDetector;
    }

    public Bitmap gridFullObject(Bitmap bmpImage, boolean model){
        for(int i = 0; i < bmpImage.getWidth(); i++){
            for (int j = 0; j < bmpImage.getHeight(); j++){
                if(bmpImage.getPixel(i, j) == Color.WHITE && model){
                    gridDetection(bmpImage,model);
                    bmpImage = removeObject(bmpImage, i, j, model);
                }
                else if (bmpImage.getPixel(i, j) == Color.BLACK && !model){
                    gridDetection(bmpImage,model);
                    bmpImage = removeObject(bmpImage, i, j, model);
                }
            }
        }
        return bmpImage;
    }

    public static int getGrayVal(Bitmap greyscaleImage, int x, int y){
        int p = greyscaleImage.getPixel(x, y);
        int R = (p & 0xff0000) >> 16;
        int G = (p & 0x00ff00) >> 8;
        int B = (p & 0x0000ff) >> 0;
        double GRAY = (0.299 * R)  + (0.587 * G) + (0.114 * B);
        return (int) GRAY;
    }

    public static Bitmap edgeHomogen(Bitmap bmpImage){
        System.out.println("Edge Homogen");
        Bitmap grayBitmap = toGrayscale(bmpImage);
        int width, height;
        height = grayBitmap.getHeight();
        width = grayBitmap.getWidth();
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);



        for (int i = 1; i < width-1; i++){
            for (int j = 1; j < height-1; j++){
                int grayValue = getGrayVal(grayBitmap, i, j);
                //System.out.println("Pixel Center: (" + i + " , " + j +")");
                //System.out.println("Old Gray Value: " + grayValue);
                int maxDiff = 0;
                for (int k = i-1; k < i+2; k ++){
                    for (int l = j-1; l < j+2 ; l++){
                        int grayValueN = getGrayVal(grayBitmap, k, l);
                        //System.out.println("Pixel Neighbor: (" + k + " , " + l +")");
                        //System.out.println("Neighbor Value: " + grayValueN);
                        int diff = Math.abs(grayValue - grayValueN);
                        if (diff > maxDiff){
                            maxDiff = diff;
                        }
                    }
                }
                //System.out.println("New Gray Value: " + Color.rgb(maxDiff, maxDiff, maxDiff));
                tempBitmap.setPixel(i, j, Color.rgb(maxDiff, maxDiff, maxDiff));
            }
        }
        return tempBitmap;
    }

    public static Bitmap edgeDifference(Bitmap bmpImage){
        System.out.println("Edge Difference");
        Bitmap grayBitmap = toGrayscale(bmpImage);
        int width, height;
        height = grayBitmap.getHeight();
        width = grayBitmap.getWidth();
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int i = 1; i < width-1; i++){
            for (int j = 1; j < height-1; j++){
                int maxDiff = 0;
                //Kiri atas kanan bawah
                int upLeft = getGrayVal(grayBitmap, i - 1, j - 1);
                int botRight = getGrayVal(grayBitmap, i + 1, j + 1);
                // atas bawah
                int upMid = getGrayVal(grayBitmap, i - 1, j);
                int botMid = getGrayVal(grayBitmap, i + 1, j);
                // kanan atas kiri bawah
                int upRight = getGrayVal(grayBitmap, i - 1, j + 1);
                int botLeft = getGrayVal(grayBitmap, i + 1, j - 1);
                //kiri kanan
                int midLeft = getGrayVal(grayBitmap, i, j - 1);
                int midRight = getGrayVal(grayBitmap, i, j + 1);

                int diff = Math.abs(upLeft - botRight);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                diff = Math.abs(upMid - botMid);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                diff = Math.abs(upRight - botLeft);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                diff = Math.abs(midLeft - midRight);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
                //System.out.println("New Gray Value: " + Color.rgb(maxDiff, maxDiff, maxDiff));
                tempBitmap.setPixel(i, j, Color.rgb(maxDiff, maxDiff, maxDiff));

            }
        }
        return tempBitmap;
    }

    public static Bitmap oneDegree (Bitmap bmpImage, int choice){
        int[][] position = {{-1, -1}, {-1, 0,}, {-1, 1}, {0, -1},{0, 0}, {0, 1}, {1, -1},{1 , 0},{1, 1}};
        int[] maskPrewittV = {1,0,-1,1,0,-1,1,0,-1};
        int[] maskPrewittH = {1,1,1,0,0,0,-1,-1,-1};
        int[] maskSobelV = {1,0,-1,2,0,-2,1,0,-1};
        int[] maskSobelH = {1,2,1,0,0,0,-1,-2,-1};
        int[] maskFreiChenV = {1,0,-1, (int) Math.sqrt(2), 0 , (int) (Math.sqrt(2) * -1), 1, 0, -1 };
        int[] maskFreiChenH = {1, (int) Math.sqrt(2), 1, 0,0,0,-1, (int) (Math.sqrt(2) * -1), -1};
        int[] maskV;
        int[] maskH;
        switch (choice){
            case 0:
                maskV = maskPrewittV;
                maskH = maskPrewittH;
                break;
            case 1:
                maskV = maskSobelV;
                maskH = maskSobelH;
                break;
            case 2:
                maskV = maskFreiChenV;
                maskH = maskFreiChenH;
                break;
            default:
                maskV = maskPrewittV;
                maskH = maskPrewittH;
                break;
        }
        Bitmap grayBitmap = toGrayscale(bmpImage);
        int width, height;
        height = grayBitmap.getHeight();
        width = grayBitmap.getWidth();
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int i = 1; i < width-1; i++){
            for (int j = 1; j < height-1; j++){
                int hValueSum = 0;
                int vValueSum = 0;
                for (int k = 0; k < position.length; k ++){
                    int posH = j + position[k][0];
                    int posV = i + position[k][1];
                    int pixValue = getGrayVal(grayBitmap, posV, posH);
                    int factorH = maskH[k];
                    int newGrayValH = factorH * pixValue;
                    int factorV = maskV[k];
                    int newGrayValV = factorV * pixValue;

                    hValueSum = hValueSum + newGrayValH;
                    vValueSum = vValueSum + newGrayValV;
                }
                int val = (int) Math.sqrt((hValueSum*hValueSum) + (vValueSum * vValueSum));
//                if (hValueSum >= vValueSum){
//                    val = hValueSum;
//                } else {
//                    val = vValueSum;
//                }
                if (val > 255){
                    val = 255;
                }
                if (val < 0 ){
                    val = 0;
                }
                tempBitmap.setPixel(i,j, Color.rgb(val,val,val));
            }
        }
        return tempBitmap;
    }
    public static Bitmap twoDegree (Bitmap bmpImage, int choice){
        int[][] position = {{-1, -1}, {-1, 0,}, {-1, 1}, {0, -1},{0, 0}, {0, 1}, {1, -1},{1 , 0},{1, 1}};
        int[] maskRobinsonN = {-1,0,1,-2,0,2,-1,0,1};
        int[] maskRobinsonNE = {-2,-1,0,-1,0,1,0,1,2};
        int[] maskRobinsonE = {-1,-2,-1,0,0,0,1,2,1};
        int[] maskRobinsonSE = {0,-1,-2,1,0,-1,2,1,0};
        int[] maskRobinsonS = {1,0,-1,2,0,-2,1,0,-1};
        int[] maskRobinsonSW = {2,1,0,1,0,-1,0,-1,-2};
        int[] maskRobinsonW = {1,2,1,0,0,0,-1,-2,-1};
        int[] maskRobinsonNW = {0,1,2,-1,0,1,-2,-1,0};
        int[][] maskRobinson = {maskRobinsonN, maskRobinsonNE, maskRobinsonE, maskRobinsonSE, maskRobinsonS, maskRobinsonSW, maskRobinsonW, maskRobinsonNW};

        int[] maskKirschN = {-3,-3,5,-3,0,5,-3,-3,5};
        int[] maskKirschNE = {-3,-3,-3,-3,0,5,-3,5,5};
        int[] maskKirschE = {-3,-3,-3,-3,0,-3,5,5,5};
        int[] maskKirschSE = {-3,-3,-3,5,0,-3,5,5,-3};
        int[] maskKirschS = {5,-3,-33,5,0,-3,5,-3,-3};
        int[] maskKirschSW = {5,5,-3,5,0,-3,-3,-3,-3};
        int[] maskKirschW = {5,5,5,-3,0,-3,-3,-3,-3};
        int[] maskKirschNW = {-3,5,5,-3,0,5,-3,-3,-3};
        int[][] maskKirsch = {maskKirschN, maskKirschNE, maskKirschE, maskKirschSE, maskKirschS, maskKirschSW,maskKirschW,maskKirschNW};
        int[][] mask;
        switch (choice){
            case 0:
                mask = maskRobinson;
                break;
            case 1:
                mask = maskKirsch;
            default:
                mask = maskRobinson;
                break;
        }

        Bitmap grayBitmap = toGrayscale(bmpImage);
        int width, height;
        height = grayBitmap.getHeight();
        width = grayBitmap.getWidth();
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 1; i < width-1; i++){
            for (int j = 1; j < height-1; j++){
                int[] valueSum = {0,0,0,0,0,0,0,0};
                for (int k = 0; k < position.length; k ++){
                    int posH = j + position[k][0];
                    int posV = i + position[k][1];
                    int pixValue = getGrayVal(grayBitmap, posV, posH);
                    int[] factor = new int[8];
                    for (int l = 0; l < mask.length; l++){
                        factor[l] = mask[l][k];
                    }
                    int[] newGrayVal = new int[8];
                    for (int l = 0; l < factor.length; l++){
                        newGrayVal[l] = factor[l] * pixValue;
                    }
                    for (int l =0; l < newGrayVal.length; l++){
                        valueSum[l] = valueSum[l] + newGrayVal[l];
                    }
                }
                int val = 0;
                for (int k = 0; k < valueSum.length; k++){
                    if (valueSum[k] > val){
                        val = valueSum[k];
                    }
                }
                if (val > 255){
                    val = 255;
                }
                if (val < 0 ){
                    val = 0;
                }
                tempBitmap.setPixel(i,j, Color.rgb(val,val,val));
            }
        }


        return tempBitmap;
    }

    public static Bitmap clusterFace(Bitmap bmpImage){
        Point[] facePixels;
        ArrayList<Point> face = new ArrayList<>();
        for(int i = 0; i < bmpImage.getWidth(); i++){
            for(int j = 0; j < bmpImage.getHeight(); j++){
                if(bmpImage.getPixel(i,j) == Color.BLACK){
                    face.add(new Point(i, j));
                }
            }
        }
        facePixels = new Point[face.size()];
        for(int i = 0; i < face.size(); i++){
            facePixels[i] = face.get(i);
        }

        Kmeans clusterer = new Kmeans(facePixels, 4, 100);
        clusterer.categorizePixels();

        ArrayList<ArrayList<Point>> clusteredPixels = new ArrayList<>();
        clusteredPixels = clusterer.getCategorizedPixels();

        int color[] = {Color.RED,  Color.BLUE, Color.GREEN, Color.MAGENTA};
        for(int i = 0; i < 4; i++){
            for(int index = 0; index < clusteredPixels.get(i).size(); index++){
                bmpImage.setPixel(clusteredPixels.get(i).get(index).getX(), clusteredPixels.get(i).get(index).getY(), color[i]);
            }
        }

        return bmpImage;
    }
}

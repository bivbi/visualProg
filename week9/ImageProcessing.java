package ch.epfl.cs211;
// Switch workspace ???

import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet
{
    static String imgName = "board1.jpg";
    
    static int[][]Kernel1 = {{0,0,0},{0,2,0},{0,0,0}};
    static int[][]Kernel2 = {{0,1,0},{1,0,1},{0,1,0}};
    static int[][]GausianBlur = {{9,12,9},{12,15,12},{9,12,9}};
    
    static int[][] hKernel = { { 0, 1, 0 }, {0, 0,0}, { 0, -1, 0 } };      
    static int[][] vKernel = { { 0, 0, 0 }, {1, 0,-1}, {0, 0, 0 } };
    
    PImage imgLoaded;
    PImage imgResult;
    PImage imgResult2;
    HScrollbar thresholdBar;
    HScrollbar colorMinBar;
    HScrollbar colorMaxBar;
    HScrollbar weightBar;
    int threshold;    
    float weight;
    float colorMin;
    float colorMax;
    
    public void setup() {
        size(1200, 600);    // 1600, 600
        imgLoaded = loadImage(imgName);
        thresholdBar = new HScrollbar(this, 0, 580, 1200, 20);      // 0, 580, 1600, 20
        weightBar = new HScrollbar(this, 0, 555, 1200, 20);
        colorMinBar = new HScrollbar(this, 0, 500, 1200, 20);
        colorMaxBar = new HScrollbar(this, 0, 525, 1200, 20);
        // noLoop(); // no interactive behaviour: draw() will be called only once.
    }
    
    public void draw() {
        imgLoaded = loadImage(imgName);
        
        colorMin = colorMinBar.getPos() * 255;
        colorMax = colorMaxBar.getPos() * 255;
        threshold = (int)(thresholdBar.getPos() * 255);
        weight = weightBar.getPos() * 100;
        
//        imgResult = thresholding(imgLoaded);
//        imgResult2 = thresholding2(imgLoaded);
        imgResult = hueThresholding(imgLoaded);
//        imgResult = convolute(imgLoaded, GausianBlur);
//        imgResult = convolute(convolute(imgLoaded, hKernel), vKernel);
//        imgResult = convolute(imgLoaded, hKernel);
//        imgResult2 = convolute(imgLoaded, vKernel);
        
//        PImage imgCopy;    // imgLoaded.copy(imgCopy, 0, 0, imgLoaded.width, imgLoaded.height, 0, 0, imgLoaded.width, imgLoaded.height);   // COPIE de l'image ??? Où ???
//        try { imgCopy = (PImage)imgLoaded.clone();
//                imgResult = sobel(imgCopy);
//        } catch (CloneNotSupportedException e) { e.printStackTrace(); }
        
        image(imgLoaded, 0, 0);
        image(hueThresholding((convolute(imgLoaded, GausianBlur))), width/2, 0);
        
        // Test (TODO: à la main)   vérifier que le gaussian blur est bien la moyenne pondéré (9, 12, 15...) des pixels voisins !
//        System.out.println("("+red(imgLoaded.pixels[123])+", "+green(imgLoaded.pixels[123])+", "+blue(imgLoaded.pixels[123])+")");
//        System.out.println("("+red(imgResult.pixels[123])+", "+green(imgResult.pixels[123])+", "+blue(imgResult.pixels[123])+")");
        // Brightness (une couleur rgb) == la valeur du canal BLUE
        
//        thresholdBar.display(); thresholdBar.update();
        weightBar.display(); weightBar.update();
        colorMinBar.display(); colorMinBar.update();
        colorMaxBar.display(); colorMaxBar.update();
        
        text("word", 10, 30); 
        fill(255, 255, 255);        // Blanc ??? 
    }
        
    public void keyPressed() {
        if (key == '1') {
            imgName = "board1.jpg";
        } else if (key == '2') {
            imgName = "board2.jpg";
        } else if (key == '3') {
            imgName = "board3.jpg";
        } else if (key == '4') {
            imgName = "board4.jpg";
        }
    }

/*    public PImage thresholding2(PImage img) {
        PImage result = createImage(img.width, img.height, RGB);  // create a new, initially transparent, 'result' image
        for(int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = (brightness(img.pixels[i]) < (int)threshold) ? 0x000000 : 0xFFFFFF;
        }
        result.updatePixels();
        return result;
    } */
    
    public PImage thresholding(PImage img) {       // Real one ! --> threshold with regards to gray scale, and not BLUE channel !!!
        PImage result = createImage(img.width, img.height, RGB);
        img.filter(GRAY);
        img.loadPixels(); result.loadPixels();
        for(int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = (brightness(img.pixels[i]) < (int)threshold) ? 0x000000 : 0xFFFFFF;
        }
        result.updatePixels();
        return result;
    }
    
    public PImage hueThresholding(PImage img) {
        PImage result = createImage(img.width, img.height, RGB);
        result.loadPixels();
        for (int i=0; i<img.width * img.height; ++i) {      // Les noms des 2 bars devraient être colorMinBar  et  colorMaxBar !!! √
            if (colorMin <= hue(img.pixels[i]) && hue(img.pixels[i]) <= colorMax) {
                result.pixels[i] = img.pixels[i];
            } else {
                result.pixels[i] = color(0, 0, 0);      // Si la couleur est dans l'intervalle des 2 barres, on la garde, sinon --> Noir
            }
        }
        result.updatePixels();
        return result;
    }
  
    public PImage convolute(PImage img, int[][] Kernel) {
        PImage result = createImage(img.width, img.height, ALPHA);
        // img.pixels is a linear array --> img[i][j] == img.pixels[i + img.width*j];
        
//        float weight = 1.f;
        
        for(int x=0; x<img.width; x++) {
            for(int y=0; y<img.height; y++) {
                int reds = 0;
                int greens = 0;
                int blues = 0;
                for(int i=0; i<Kernel.length; i++) {
                    for(int j=0; j<Kernel[0].length; j++) {
                            int clampedX = (x+i-1 < 0)  ?  0 :  ((x+i-1 > img.width-1) ? img.width-1 : x+i-1);
                            int clampedY = (y+j-1 < 0)  ?  0 :  ((y+j-1 > img.height-1) ? img.height-1 : y+j-1);
                            reds += red(img.pixels[clampedX + img.width*clampedY]) * Kernel[i][j];
                            greens += green(img.pixels[clampedX + img.width*clampedY]) * Kernel[i][j];
                            blues += blue(img.pixels[clampedX + img.width*clampedY]) * Kernel[i][j];
                    }
                }
                result.pixels[x + img.width*y] = color(reds/weight, greens/weight, blues/weight);    // divisé par 9.0 ??? Ferait exactement la moyenne arithmétique
            }
        }
        result.updatePixels();
        return result;
    }
    
    
    // TODO: comparer avec méthode sobel de Virgile ! (Git)
    public PImage sobel(PImage img) {
      
        int[][] hKernel = { { 0, 1, 0 }, {0, 0,0}, { 0, -1, 0 } };      
        int[][] vKernel = { { 0, 0, 0 }, {1, 0,-1}, {0, 0, 0 } };
        
        img.filter(GRAY);   // Do SOBEL only on greyscale images !!! In order to brigtness() not te return the value of BLUE channel !!!
        
        PImage result = createImage(img.width, img.height, ALPHA);
        result.loadPixels();
        for (int i = 0; i < img.width * img.height; i++) {  // Clear the image
          result.pixels[i] = color(0);
        }
        
        double max=0;
        double[] buffer = new double[img.width * img.height];

        // Does the double convolution :
        for(int x=0; x<img.width; x++) {
            for(int y=0; y<img.height; y++) {
                int sum_h = 0;
                int sum_v = 0;
                for(int i=0; i<hKernel.length; i++) {
                    for(int j=0; j<hKernel[0].length; j++) {
                            int clampedX = (x+i-1 < 0)  ?  0 :  ((x+i-1 > img.width-1) ? img.width-1 : x+i-1);
                            int clampedY = (y+j-1 < 0)  ?  0 :  ((y+j-1 > img.height-1) ? img.height-1 : y+j-1);
                            sum_h += brightness(img.pixels[clampedX + img.width*clampedY]) * hKernel[i][j];
                            sum_v += brightness(img.pixels[clampedX + img.width*clampedY]) * vKernel[i][j];
                    }
                }
                // TODO: sum_h et sum_v divisées par weight ??? A quoi sert ??? --> Moyenne pondérée, donc divisé par 9 ??? (taille du Kernel)
                double sum = Math.sqrt(pow(sum_h, 2) + pow(sum_v, 2));  // euclidian distance
                buffer[x+img.width*y] = sum;
                max = (sum > max) ? sum : max;
            }
        }
        
        for (int y = 2; y < img.height - 2; y++) {                            // Skip top and bottom edges
            for (int x = 2; x < img.width - 2; x++) {                       // Skip left and right
                if (buffer[y * img.width + x] > (int)(max * 0.3f)) {   // 30% of the max
                    result.pixels[y * img.width + x] = color(255);
                } else {
                    result.pixels[y * img.width + x] = color(0);
                }
            }
        }
        
        result.updatePixels();      // Always call loadPixels() --> DO STUFF --> updatePixels() --> return.
        return result;
    }
    

    public void hough(PImage edgeImg) {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;
        
        // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
        
        // our accumulator (with a 1 pix margin around)
        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
        
        // Fill the accumulator: on edge points (ie, white pixels of the edge image), store all possible (r, phi) pairs
        // describing lines going through the point.
        for (int y = 0; y < edgeImg.height; y++) {
            for (int x = 0; x < edgeImg.width; x++) {
                // Are we on an edge?
                if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                              
                    // ...determine here all the lines (r, phi) passing through pixel (x,y), convert (r,phi) to coordinates
                    // in the accumulator, and increment accordingly the accumulator.
                    
                }
            }
        }
        
        // Display accumulator image :
        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
        for (int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }
        houghImg.updatePixels();
    }
    
    
}
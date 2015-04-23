package ch.epfl.cs211;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class Webcam extends PApplet {
    
    Capture cam;
    PImage img;
    
    static int[][]GaussianBlurKernel = {{9,12,9},{12,15,12},{9,12,9}};
    static int[][]IdentityKernel = {{0,0,0},{0,1,0},{0,0,0}};


    public void setup() {
        size(640, 480);
        String[] cameras = Capture.list();
        if (cameras.length == 0) {
            println("There are no cameras available for capture.");
            exit();
        } else {
            println("Available cameras:");
            for (int i = 0; i < cameras.length; i++) {
                println(cameras[i]);
            }
            cam = new Capture(this, cameras[0]);
            cam.start();
        }
    }

    public void draw() {
        if (cam.available() == true) {
            cam.read();
        }
        img = cam.get();
        image(sobel(img), 0, 0);
    }
    
    public PImage convolute(PImage img, int[][] Kernel) {
        PImage result = createImage(img.width, img.height, ALPHA);
        
        float weight = 0.4f;
        
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
                if (buffer[y * img.width + x] > (int)(max * 0.15f)) {   // 15% of the max
                    result.pixels[y * img.width + x] = color(255);
                } else {
                    result.pixels[y * img.width + x] = color(0);
                }
            }
        }
        
        result.updatePixels();      // Always call loadPixels() --> DO STUFF --> updatePixels() --> return.
        return result;
    }
}

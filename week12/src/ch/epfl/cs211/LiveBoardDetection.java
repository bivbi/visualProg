package ch.epfl.cs211;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class LiveBoardDetection extends PApplet {
    
    Capture cam;
    PImage img;
    
    static int[][]GaussianBlurKernel = {{9,12,9},{12,15,12},{9,12,9}};
    static int[][]BoxBlurKernel = {{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1}};

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
        PImage imgResult = hueThresholding(img, 90, 145);
        PImage imgResult2 = sobel(imgResult, 0.5);
        image(imgResult, 0, 0);     // imageResult2 for green-object edge detection !     
    }
    
    // ====================================== // STATIC API ???? --> From BoardDetection
    
    /**
     * Performs a thresholding operation on the given image, with the
     * specified threshold.
     * 
     * @param img - the image to be thresholded
     * @param threshold - between 0 and 255
     * 
     * @return a black and white image, result of the thresholding operation.
     */
    public PImage thresholding(PImage img, int threshold) {
        PImage result = createImage(img.width, img.height, ALPHA);
        img.filter(GRAY);   // WARNING: TODO: copy of the img !
        img.loadPixels(); result.loadPixels();
        for(int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = (brightness(img.pixels[i]) < threshold) ? color(0) : color(255);
        }   // 0 : White   and   255: Black
        result.updatePixels();
        return result;
    }
    
    /**
     * Performs a hue thresholding operation on the given image, with the
     * specified min and max hue (between 0 and 255)
     * 
     * @param img - the image to be hue thresholded
     * @param colorMin - minimum hue to be kept
     * @param colorMax - maximum hue to be kept
     * 
     * @return a black and white image, where all the pixels from <tt>img</tt>
     * whose hue ⋲ [<tt>colorMin</tt>, <tt>colorMax</tt>] are painted in white, and
     * the others painted in black.
     */
    public PImage hueThresholding(PImage img, float colorMin, float colorMax) {
        PImage result = createImage(img.width, img.height, ALPHA);
        result.loadPixels();
        for (int i=0; i<img.width * img.height; ++i) {
            if (colorMin <= hue(img.pixels[i]) && hue(img.pixels[i]) <= colorMax) {
                result.pixels[i] = color(0);
            } else {
                result.pixels[i] = color(255);
            }
        }
        result.updatePixels();
        return result;
    }

    private int sumWeight(int[][] Kernel) {
        int sum = 0;
        for(int i=0; i<Kernel.length; i++) {
            for(int j=0; j<Kernel[i].length; j++)
                sum += Kernel[i][j];
        }
        return sum;
    }
    
    /**
     * Performs a simple normalized convolution operation on the given image.
     * 
     * @param img - the image to be convoluted
     * @param Kernel - the Kernel to convolute the image with
     * 
     * @return a new <tt>PImage</tt>, result of the normalized convolution of
     * the image with the kernel passed as parameters. 
     */
    public PImage convolutionNormalized(PImage img, int[][] Kernel) {
        int normalizingWeight = sumWeight(Kernel);
        return convolutionWithWeight(img, Kernel, normalizingWeight);
    }
    
    /**
     * Performs a simple normalized convolution operation on the given image,
     * using the specified weight.
     * 
     * @param img - the image to be convoluted
     * @param Kernel - the Kernel to convolute the image with
     * @param weight - the divisor of all summed intensities
     * 
     * @return a new <tt>PImage</tt>, result of the convolution of the image
     * with the kernel passed as parameters. 
     */
    // Perfect EXCEPT the edges !!! --> What to do with it ??? cf. http://beej.us/blog/data/convolution-image-processing/
    public PImage convolutionWithWeight(PImage img, int[][] Kernel, int weight) {
        PImage result = createImage(img.width, img.height, RGB);
        
        for(int x=Kernel.length/2; x<img.width-Kernel.length/2; x++) {
            for(int y=Kernel.length/2; y<img.height-Kernel.length/2; y++) {
                int reds = 0;
                int greens = 0;
                int blues = 0;
                                
                int rasterPosition = x + y*img.width;
                
                for(int i=-Kernel.length/2; i<=Kernel.length/2; i++) {
                  for(int j=-Kernel.length/2; j<=Kernel.length/2; j++) {
                      reds += red(img.pixels[rasterPosition + i*img.width + j]) * Kernel[i+Kernel.length/2][j+Kernel.length/2];
                      blues += blue(img.pixels[rasterPosition + i*img.width + j]) * Kernel[i+Kernel.length/2][j+Kernel.length/2];
                      greens += green(img.pixels[rasterPosition + i*img.width + j]) * Kernel[i+Kernel.length/2][j+Kernel.length/2];
                  }
                }
                
                result.pixels[rasterPosition] = color(reds/weight, greens/weight, blues/weight); 
            }
        }
        result.updatePixels();
        return result;
    }
    
    /**
     * Performs an edge detection operation (using Sobel operator) on the given
     * image. WARNING : This image will be modified BLACK AND WHITE / GREYSCALE...
     * 
     * @param img - the image for which we want to detect edges
     *  @param threshold - intensity of the edge detection, between 0 and 1
     *  
     * @return
     */
    public PImage sobel(PImage img, double threshold) {
            
        int[][] hKernel = {{0,1,0},{0,0,0},{0,-1,0}};      
        int[][] vKernel = {{0,0,0},{1,0,-1},{0,0,0}};
        
        float weight = 1.f;     // A quoi ça sert ?
        
        img.filter(GRAY);   // Do SOBEL only on greyscale images !!! In order to brigtness() not te return the value of BLUE channel !!!
        // WARNING : TODO: Copy of the img ??? In order not to modify it ! √ Definitely.
        
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
                // sum_h /= weight; sum_v /= weight;
                double sum = Math.sqrt(pow(sum_h, 2) + pow(sum_v, 2));  // euclidian distance
                buffer[x+img.width*y] = sum;
                max = (sum > max) ? sum : max;
            }
        }
        
        for (int y = 2; y < img.height - 2; y++) {
            for (int x = 2; x < img.width - 2; x++) {
                if (buffer[y * img.width + x] > (int)(max * threshold)) {
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

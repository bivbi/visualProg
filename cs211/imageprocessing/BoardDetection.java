package ch.epfl.cs211;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class performing the (live) detection of a green lego board
 *
 * @author Alexandre Connat
 * @author Yann Dupont-Costedoat
 * @author Virgile Neu
 */
public class BoardDetection extends PApplet {

    static public void main(String args[]) {
        PApplet.main(new String[] { "BoardDetection" });
    }

    /******************* STATIC VALUES : *****************/

    static Capture cam;
    static PImage img;
    static String imgName = "board1.jpg";

    // Valid for duplo boards (boardX.jpg images --> NOT FOR WEBCAM)
    static float minHue = 90;
    static float maxHue = 138;
    static float minSat = 60;
    static float maxSat = 255;
    static float minBright = 34;
    static float maxBright = 167;

    
    // Our lego board is lighter (greener) and brighter than the Duplo board on the images.
    // Much more like this : (in a bright room)
//    static float minHue = 80;
//    static float maxHue = 133;
//    static float minSat = 65;
//    static float maxSat = 255;
//    static float minBright = 40;
//    static float maxBright = 200;

    static float sobelThreshold = 0.3f;

    static Boolean webcam = false;
    static Boolean displayOn = true;

//    /* Use it to test values for Hue/Saturation/Brightness */
//    static HScrollbar hueMinBar;
//    static HScrollbar hueMaxBar;
//    static HScrollbar satMinBar;
//    static HScrollbar satMaxBar;
//    static HScrollbar brightMinBar;
//    static HScrollbar brightMaxBar;

    static int[][] GaussianBlurKernel = {{9,12,9},{12,15,12},{9,12,9}};
    static int[][] BoxBlurKernel = {{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1}};

    /***********************************************/

    public void setup() {
        size(1200, 300); // change if needed
        img = loadImage(imgName); //press 1-2-3-4 to get the images, w for webcam

//        /* Use for testing */
//        hueMinBar = new HScrollbar(this, 0, 430, 800, 20);
//        hueMaxBar = new HScrollbar(this, 0, 460, 800, 20);
//        satMinBar = new HScrollbar(this, 0, 490, 800, 20);
//        satMaxBar = new HScrollbar(this, 0, 520, 800, 20);
//        brightMinBar = new HScrollbar(this, 0, 550, 800, 20);
//        brightMaxBar = new HScrollbar(this, 0, 580, 800, 20);
        
        getMinMaxBoundaries();
        if (webcam) {
           getCam();
        }
    }

	public void draw() {

        if (webcam) {
            if (cam.available()) {
                cam.read();
            }
            img = cam.get();
        }

        PGraphics render = createGraphics(2400, 600);
        render.beginDraw();
        render.background(0);

        PImage img1 = HSBfiltering(img, minHue, maxHue, minSat, maxSat, minBright, maxBright);
        PImage imgB = convolutionNormalized(img1, GaussianBlurKernel);
        PImage img2 = thresholding(imgB, 0.1f);
        PImage imgSobel = sobel(img2, sobelThreshold);

        int[] accumulator = hough(imgSobel);

        // Get lines and intersections
        ArrayList<PVector> linesCoordinates = getCoordinates(imgSobel, accumulator, 4);
        ArrayList<PVector> intersect = getIntersections(linesCoordinates);
        PGraphics graph = drawLines(linesCoordinates, img);

        // Display the 3 images inside the PGraphics
        render.image(drawIntersection(intersect, graph), 0, 0);
        render.image(drawAccumulator(imgSobel, accumulator), 800, 0);
        render.image(imgSobel, 1600, 0);

        render.endDraw();
        render.resize(1200, 300);
        image(render, 0, 0);
    }


//    /* Draw() with Scrollbars (use for testing) : */
//    public void draw() {
//
//        if (webcam) {
//            if (cam.available()) {
//                cam.read();
//            }
//            img = cam.get();
//        }
//
//        float minHue = hueMinBar.getPos()*255;
//        float maxHue = hueMaxBar.getPos()*255;
//        float minSat = satMinBar.getPos()*255;
//        float maxSat = satMaxBar.getPos()*255;
//        float minBright = brightMinBar.getPos()*255;
//        float maxBright = brightMaxBar.getPos()*255;
//
//        PImage img1 = HSBfiltering(img, minHue, maxHue, minSat, maxSat, minBright, maxBright);
//        PImage imgB = convolutionNormalized(img1, GaussianBlurKernel);
//        PImage img2 = thresholding(imgB, 10);
//        PImage imgSobel = sobel(img2, 0.3);
//
//        image(imgSobel,0,0);
//
//        if (displayOn) {
//            fill(204, 102, 0);
//            text("Hmin = " + minHue, 10, 10);
//            text("Hmax = " + maxHue, 10, 30);
//            text("Smin = " + minSat, 10, 50);
//            text("Smax = " + maxSat, 10, 70);
//            text("Bmin = " + minBright, 10, 90);
//            text("Bmax = " + maxBright, 10, 110);
//            hueMinBar.display(); hueMinBar.update();
//            hueMaxBar.display(); hueMaxBar.update();
//            satMinBar.display(); satMinBar.update();
//            satMaxBar.display(); satMaxBar.update();
//            brightMinBar.display(); brightMinBar.update();
//            brightMaxBar.display(); brightMaxBar.update();
//        }
//    }


    public void keyPressed() {
        // Just for testing (display or not the HScrollbars)
        if (key == 'd') {
            displayOn = false;
        } else if (key == 'D') {
            displayOn = true;
        } else if(!webcam) {
        	if(key == 'w') {
        		webcam = true;
        		getMinMaxBoundaries();
        		if(cam == null) {
        			getCam();
        		} else {
        			cam.start();
        		}
        	} else if (key == '1') {
	            imgName = "board1.jpg";
	        } else if (key == '2') {
	            imgName = "board2.jpg";
	        } else if (key == '3') {
	            imgName = "board3.jpg";
	        } else if (key == '4') {
	            imgName = "board4.jpg";
	        }
        } else if(webcam && key == 'w') {
        	webcam = false;
        	getMinMaxBoundaries();
        	cam.stop();
        }

        img = loadImage(imgName);
    }




    /***************************************************/
    /************** UTILITY METHODS : *****************/
    /**************************************************/

    /**
     * Called when we toggle on or off the cam to update the boundaries
     * because the buplo is darker than the lego.
     * Called when 'w' is pressed
     */
    private void getMinMaxBoundaries() {
    	// Valid for duplo boards (boardX.jpg images --> NOT FOR WEBCAM)
        if(!webcam) {
	    	minHue = 90;
	        maxHue = 138;
	        minSat = 60;
	        maxSat = 255;
	        minBright = 34;
	        maxBright = 167;
        } else {
	        // Our lego board is lighter (greener) and brighter than the Duplo board on the images.
	        // Much more like this : (in a bright room)
	        minHue = 80;
	        maxHue = 133;
	        minSat = 65;
	        maxSat = 255;
	        minBright = 40;
	        maxBright = 200;
        }
	}

    /**
     * Toggle on or off the cam.
     * Called when 'w' is pressed
     */
    private void getCam() {
    	String[] cameras = Capture.list();
        if (cameras.length == 0) {
        	println("There are no cameras available for capture.");
            exit();
        } else {
            println("Available cameras:");
            for (int i = 0; i < cameras.length; i++) {
                println(cameras[i]);
            }
            cam = new Capture(this, 640, 480);    // 640, 480 (instead of cameras[0])
            cam.start();
        }
	}

	/**
     * Performs a thresholding operation on the given image, with the
     * specified threshold.
     *
     * @param img - the image to be thresholded <b>WARNING :</b> will be modified !
     * @param threshold - between 0 and 255
     *
     * @return a black and white image, result of the thresholding operation.
     */
    public PImage thresholding(PImage img, float threshold) {
        PImage result = createImage(img.width, img.height, ALPHA);
        img.filter(GRAY);
        img.loadPixels(); result.loadPixels();
        for(int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = (brightness(img.pixels[i]) < threshold) ? color(0, 0, 0) : color(255, 255, 255);
        }
        result.updatePixels();
        return result;
    }

    /**
     * Performs a Hue/Saturation/Brightness filtering operation on the given image,
     * using the specified H, S, and B thresholds.
     *
     * @param img - the image to be filtered
     * @param hueMin - minimum hue to be kept in the result image
     * @param hueMax - maximum hue to be kept in the result image
     * @param satMin - minimum saturation to be kept in the result image
     * @param satMax - maximum saturation to be kept in the result image
     * @param brightMin - minimum brightness to be kept in the result image
     * @param brightMax - maximum brightness to be kept in the result image
     *
     * @return A <tt>PImage</tt>, copy of the <tt>img</tt> given as argument, but
     * where all pixels which were outside the HSB bounds were painted in black.
     */
    public PImage HSBfiltering(PImage img, float hueMin, float hueMax, float satMin, float satMax, float brightMin, float brightMax) {
        PImage result = createImage(img.width, img.height, RGB); // or HSB, same result
        result.loadPixels();
        for (int i=0; i<img.width*img.height; i++) {
            if ( (hueMin <= hue(img.pixels[i]) && hue(img.pixels[i]) <= hueMax) &&
                    (satMin <= saturation(img.pixels[i]) && saturation(img.pixels[i]) <= satMax) &&
                    (brightMin <= brightness(img.pixels[i]) && brightness(img.pixels[i]) <= brightMax) ) {
                result.pixels[i] = img.pixels[i];
            } else {
                result.pixels[i] = color(0);
            }
        }
        result.updatePixels();
        return result;
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

    private int sumWeight(int[][] Kernel) {
        int sum = 0;
        for(int i=0; i<Kernel.length; i++) {
            for(int j=0; j<Kernel[i].length; j++)
                sum += Kernel[i][j];
        }
        return sum;
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
     * with the kernel passed as parameters. <br>
     * <b>Warning :</b> The borders of the image may seem darken.
     */
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
     * image.
     *
     * @param img - the image for which we want to detect edges
     *  @param threshold - intensity of the edge detection, between 0 and 1
     *
     * @return a new ALPHA <tt>PImage</tt>, result of the Sobel edge detection algorithm
     * on the given image.
     */
    public PImage sobel(PImage img, double threshold) {

        if (img.format != ALPHA) {  // ALPHA = 4, GRAY = 12
            img.filter(GRAY);      // If the image passed as parameter is not B&W, turn it into B&W
        }

        // Assignment Kernels :
        int[][] hKernel = {{0,1,0},{0,0,0},{0,-1,0}};
        int[][] vKernel = {{0,0,0},{1,0,-1},{0,0,0}};

        // Alternative Kernels :
//        int[][] hKernel = {{-1,-2,-1},{0,0,0},{1,2,1}};
//        int[][] vKernel = {{-1,0,1},{-2,0,2},{-1,0,1}};

        float weight = 1.f;     // Here : doesn't do anything (CAN be changed !)

        PImage result = createImage(img.width, img.height, ALPHA);
        result.loadPixels();
        for (int i = 0; i < img.width * img.height; i++) {  // Clear the image
            result.pixels[i] = color(0);
        }

        double max = 0;
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
                sum_h /= weight;
                sum_v /= weight;
                double sum = Math.sqrt(pow(sum_h, 2) + pow(sum_v, 2));  // euclidian distance
                buffer[x+img.width*y] = sum;
                max = (sum > max) ? sum : max;
            }
        }

        for (int x = hKernel.length/2; x < img.width - hKernel.length/2; x++) {
            for (int y = vKernel.length/2; y < img.height - vKernel.length/2; y++) {
                if (buffer[x + y * img.width] > (int)(max * threshold)) {
                    result.pixels[x + y * img.width] = color(255);
                } else {
                    result.pixels[x + y * img.width] = color(0);
                }
            }
        }

        result.updatePixels();
        return result;
    }


    /**
     *
     * @param edgeImg - original <tt>PImage</tt> we want to detect the edges on.
     *
     * @return a new int[], accumulator of the edgeImg.
     */
    public int[] hough(PImage edgeImg) {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;

        // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        // our accumulator (with a 1 pix margin around)
        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];


        for (int y = 0; y < edgeImg.height; y++) {
            for (int x = 0; x < edgeImg.width; x++) {
                // Are we on an edge? --> Pixel is NOT black (white)
                if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    // determine and store all possible (r, phi) pairs describing lines going through the point.
                    // convert (r, phi) to coordinates in the accumulator
                    for (int phi = 0; phi < phiDim; phi++) {
                        float realPhi = phi * discretizationStepsPhi;
                        double r = x * Math.cos(realPhi) + y * Math.sin(realPhi);
                        int rAcc = (int) ((r / discretizationStepsR) + 0.5 * (rDim - 1));
                        // increment the accumulator at THESE coordinates
                        accumulator[(phi + 1) * (rDim + 2) + rAcc] += 1;
                    }
                }
            }
        }
        return accumulator;
    }

    /**
     *
     * @param edgeImg - original <tt>PImage</tt> we want to detect the edges on.
     * @param accumulator - the int[] resulting of <tt>hough</tt>
     * @param nLines - number of lines we want to take
     *
     * @return a new <tt>ArrayList</tt> of PVector with nLines tuples r, phi.
     */
    ArrayList<PVector> getCoordinates(PImage edgeImg, int[] accumulator, int nLines) {
        ArrayList<PVector> result = new ArrayList<>();
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        // take most voted line
        ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

        int minVotes = 200;

        // size of the region we search for a local maximum
        int neighbourhood = 10;
        // only search around lines with more that this amount of votes
        // (to be adapted to your image)
        for (int accR = 0; accR < rDim; accR++) {
            for (int accPhi = 0; accPhi < phiDim; accPhi++) {
                // compute current index in the accumulator
                int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
                if (accumulator[idx] > minVotes) {
                    boolean bestCandidate = true;
                    // iterate over the neighbourhood
                    for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
                        // check we are not outside the image
                        if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
                            continue;
                        for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
                            // check we are not outside the image
                            if (accR + dR < 0 || accR + dR >= rDim)
                                continue;
                            int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
                            if (accumulator[idx] < accumulator[neighbourIdx]) {
                                // the current idx is not a local maximum!
                                bestCandidate = false;
                                break;
                            }
                        }
                        if (!bestCandidate)
                            break;
                    }
                    if (bestCandidate) {
                        // the current idx *is* a local maximum
                        bestCandidates.add(idx);
                    }
                }
            }
        }
        Collections.sort(bestCandidates, new HoughComparator(accumulator));

        // Return <r,phi> arrayList
        for (int idx = 0; idx < min(nLines, bestCandidates.size()); idx++) {
            // first, compute back the (r, phi) polar coordinates:
            int accPhi = (int) (bestCandidates.get(idx) / (rDim + 2)) - 1;
            int accR = bestCandidates.get(idx) - (accPhi + 1) * (rDim + 2) - 1;
            float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
            float phi = accPhi * discretizationStepsPhi;
            result.add(new PVector(r, phi));
        }
        return result;
    }

    /**
     *
     * @param edgeImg - original <tt>PImage</tt> we want to detect the edges on.
     * @param accumulator - the int[] resulting of <tt>hough</tt>
     *
     * @return a new <tt>PImage</tt> with the accumulator resized to the edgeImg size.
     */
    PImage drawAccumulator(PImage edgeImg, int[] accumulator) {
        float discretizationStepsPhi = 0.06f;
        float discretizationStepsR = 2.5f;
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
        houghImg.loadPixels();
        for (int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }
        houghImg.updatePixels();
        houghImg.resize(edgeImg.width, edgeImg.height);
        return houghImg;
    }

    /**
     *
     * @param lines - <tt>ArrayList</tt> of PVector of tuple r, phi, result of getCoordinates.
     *
     * @return a new <tt>ArrayList</tt> containt the carthesian coordinates
     * of the intersection of the different lines in <tt>lines</tt>
     */
    private ArrayList<PVector> getIntersections(List<PVector> lines) {
        ArrayList<PVector> intersections = new ArrayList<>();
        for (int i = 0; i < lines.size() - 1; i++) {
            PVector line1 = lines.get(i);
            for (int j = i + 1; j < lines.size(); j++) {
                PVector line2 = lines.get(j);
                float r1 = line1.x, r2 = line2.x, p1 = line1.y, p2 = line2.y;
                float d = cos(p2) * sin(p1) - cos(p1) * sin(p2);
                // compute the intersection and add it to 'intersections'
                int x = (int) ((r2 * sin(p1) - r1 * sin(p2)) / d);
                int y = (int) ((-r2 * cos(p1) + r1 * cos(p2)) / d);
                intersections.add(new PVector(x, y));
            }
        }
        return intersections;
    }

    /**
    *
    * @param takenLine - <tt>ArrayList</tt> of PVector of (r, phi), result of  getCoordinates.
    * @param baseImg - <tt>PImage</tt> to display under the lines.
    *
    * @return a new <tt>PImage</tt> with the corresponding lines on it.
    */
   private PGraphics drawLines(ArrayList<PVector> takenLine, PImage baseImg) {
       PGraphics lines = createGraphics(baseImg.width, baseImg.height);
       lines.beginDraw();
       lines.background(baseImg);
       lines.stroke(204, 102, 0);
       for (int idx = 0; idx < takenLine.size(); idx++) {
           // Cartesian equation of a line: y = ax + b
           // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
           // => y = 0 : x = r / cos(phi)
           // => x = 0 : y = r / sin(phi)
           // compute the intersection of this line with the 4 borders of
           // the image
           float r = takenLine.get(idx).x;
           float phi = takenLine.get(idx).y;
           int x0 = 0;
           int y0 = (int) (r / sin(phi));
           int x1 = (int) (r / cos(phi));
           int y1 = 0;
           int x2 = width;
           int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
           int y3 = width;
           int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
           // Finally, plot the lines
           if (y0 > 0) {
               if (x1 > 0) {
                   lines.line(x0, y0, x1, y1);
               } else if (y2 > 0) {
                   lines.line(x0, y0, x2, y2);
               } else {
                   lines.line(x0, y0, x3, y3);
               }
           } else {
               if (x1 > 0) {
                   if (y2 > 0) {
                       lines.line(x1, y1, x2, y2);
                   } else {
                       lines.line(x1, y1, x3, y3);
                   }
               } else {
                   lines.line(x2, y2, x3, y3);
               }
           }
       }
       lines.endDraw();
       return lines;
   }
    
    /**
     *
     * @param interCoords - <tt>ArrayList</tt> of PVector (x,y(,z=0)), result of getIntersection.
     * @param baseImage - <tt>PImage</tt> to display under the dots.
     *
     * @return a new <tt>PImage</tt> with dots corresponding on it.
     */
    PGraphics drawIntersection(ArrayList<PVector> interCoords, PImage baseImage) {
        PGraphics intersection = createGraphics(baseImage.width, baseImage.height);
        intersection.beginDraw();
        intersection.background(baseImage);
        intersection.stroke(204, 102, 0);
        intersection.fill(255, 128, 0);
        for (PVector v : interCoords) {
            intersection.ellipse(v.x, v.y, 10, 10);
        }
        intersection.endDraw();
        return intersection;
    }

}

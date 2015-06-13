package ch.epfl.cs211;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

/**
 * Class performing the (live) detection of a green lego board
 *
 * @author Alexandre Connat
 * @author Yann Dupont-Costedoat
 * @author Virgile Neu
 */
public class ImageProcessing extends PApplet {

    static public void main(String args[]) {
        PApplet.main(new String[]{"ImageProcessing"});
    }

    /*******************
     * STATIC VALUES :
     *****************/

    static Capture cam;
    static PImage img = new PImage();
    static String imgName = "board1.jpg";

    // Valid for duplo boards (boardX.jpg images --> NOT FOR WEBCAM)
    static float minHue;
    static float maxHue;
    static float minSat;
    static float maxSat;
    static float minBright;
    static float maxBright;

    static float sobelThreshold = 0.3f;

    static Boolean webcam = true;

    static int[][] GaussianBlurKernel = {{9, 12, 9}, {12, 15, 12},
            {9, 12, 9}};
    static int[][] BoxBlurKernel = {{1, 1, 1, 1, 1}, {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}};

    /***********************************************/

   /*public void setup() {
        size(1200, 300);
        img = loadImage(imgName); // press 1-2-3-4 to get the images, w for
        // webcam
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

        PImage imgResult = doTransformations(img);

        int[] accumulator = hough(imgResult);

        // Get lines and intersections
        ArrayList<PVector> linesCoordinates = getCoordinates(imgResult,
                accumulator, 6);
        ArrayList<PVector> intersect = getIntersections(linesCoordinates);
        PGraphics graph = drawLines(linesCoordinates, img);

        ArrayList<PVector> corners = computeCycle(imgResult, linesCoordinates);
        if (corners.size() >= 4) {
            TwoDThreeD conv = new TwoDThreeD(imgResult.width, imgResult.height);
            PVector d = conv.get3DRotations(corners);
            System.out.println(degrees(d.x) + ", " + degrees(d.y) + ", " + degrees(d.z));

            // Display the 3 images inside the PGraphics
            render.image(drawIntersection(intersect, graph), 0, 0);
            render.image(displayCycle(imgResult, corners), 0, 0);
        } else {
            render.image(img, 0, 0);
        }
        render.image(drawAccumulator(imgResult, accumulator), 800, 0);
        render.image(imgResult, 1600, 0);

        render.endDraw();
        render.resize(1200, 300);
        image(render, 0, 0);
    }*/
    public PVector getRotation(PApplet app, PImage img) {
        init();
        PImage imgResult = doTransformations(app, img);

        int[] accumulator = hough(imgResult);

        // Get lines and intersections
        ArrayList<PVector> linesCoordinates = getCoordinates(imgResult, accumulator, 6);

        ArrayList<PVector> corners = computeCycle(imgResult, linesCoordinates);

        if (corners.size() < 4) return null;
        TwoDThreeD conv = new TwoDThreeD(imgResult.width, imgResult.height);
        return conv.get3DRotations(corners);
    }

    private ArrayList<PVector> computeCycle(PImage image,
                                            ArrayList<PVector> lines) {
        QuadGraph quadGraph = new QuadGraph(lines, image.width, image.height);
        List<int[]> quads = quadGraph.findCycles();
        for (int[] quad : quads) {
            if (quad.length >= 4) {
                PVector l1 = lines.get(quad[0]);
                PVector l2 = lines.get(quad[1]);
                PVector l3 = lines.get(quad[2]);
                PVector l4 = lines.get(quad[3]);
                // (intersection() is a simplified version of the
                // intersections() method you wrote last week, that simply
                // return the coordinates of the intersection between 2 lines)
                PVector c12 = intersection(l1, l2);
                PVector c23 = intersection(l2, l3);
                PVector c34 = intersection(l3, l4);
                PVector c41 = intersection(l4, l1);
                // Choose a random, semi-transparent color

                if (QuadGraph.isConvex(c12, c23, c34, c41)
                        && QuadGraph.validArea(c12, c23, c34, c41, 1000000,
                        50000)
                    // && QuadGraph.nonFlatQuad(c12, c23, c34, c41)
                        ) {

                    ArrayList<PVector> result = new ArrayList<>();
                    result.add(c12);
                    result.add(c23);
                    result.add(c34);
                    result.add(c41);
                    return result;
                }
            }
        }
        return new ArrayList<PVector>();
    }

    private PGraphics displayCycle(PImage image, ArrayList<PVector> corners) {
        PGraphics result = createGraphics(image.width, image.height);
        result.beginDraw();
        if (corners.size() >= 4) {
            Random random = new Random();
            result.fill(color(min(255, random.nextInt(300)),
                    min(255, random.nextInt(300)),
                    min(255, random.nextInt(300)), 50));
            result.quad(corners.get(0).x, corners.get(0).y, corners.get(1).x,
                    corners.get(1).y, corners.get(2).x, corners.get(2).y,
                    corners.get(3).x, corners.get(3).y);
        }
        result.endDraw();
        return result;
    }

    public void keyPressed() {
        if (!webcam) {
            if (key == 'w') {
                webcam = true;
                getMinMaxBoundaries();
                if (cam == null) {
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
        } else if (webcam && key == 'w') {
            webcam = false;
            getMinMaxBoundaries();
            cam.stop();
        }
        img = loadImage(imgName);
    }

    /***************************************************/
    /************** UTILITY METHODS : *****************/
    /**************************************************/

    public PImage doTransformations(PApplet app, PImage image) {
        this.init();
        return sobel(app, intensityThresholding(app, blur(app, HSBfiltering(app, image))),
                sobelThreshold);
    }

    /**
     * Called when we toggle on or off the cam to update the boundaries because
     * the buplo is darker than the lego. Called when 'w' is pressed
     */
    public void getMinMaxBoundaries() {
        // Valid for duplo boards (boardX.jpg images --> NOT FOR WEBCAM)
        if (!webcam) {
            minHue = 90;
            maxHue = 140;
            minSat = 70;
            maxSat = 255;
            minBright = 30;
            maxBright = 180;
        } else {
            // Our lego board is lighter (greener) and brighter than the Duplo
            // board on the images.
            // Much more like this : (in a bright room)
            minHue = 90;
            maxHue = 140;
            minSat = 70;
            maxSat = 255;
            minBright = 30;
            maxBright = 180;
        }
    }

    /**
     * Toggle on or off the cam. Called when 'w' is pressed
     */
    public void getCam() {
        img = loadImage(imgName);
        getMinMaxBoundaries();
        String[] cameras = Capture.list();
        if (cameras.length == 0) {
            println("There are no cameras available for capture.");
            exit();
        } else {
            println("Available cameras:");
            for (int i = 0; i < cameras.length; i++) {
                println(cameras[i]);
            }

            cam = new Capture(this, 640, 480); // 640, 480 (instead of
            // cameras[0])
            cam.start();
        }
    }

    /**
     * Performs a thresholding operation on the given image, with the specified
     * threshold.
     *
     * @param img       - the image to be thresholded <b>WARNING :</b> will be
     *                  modified !
     * @param threshold - between 0 and 255
     * @return a black and white image, result of the thresholding operation.
     */
    public PImage thresholding(PImage img, float threshold) {
        PImage result = createImage(img.width, img.height, ALPHA);
        img.filter(GRAY);
        img.loadPixels();
        result.loadPixels();
        for (int i = 0; i < img.width * img.height; i++) {
            result.pixels[i] = (this.brightness(img.pixels[i]) < threshold) ? color(
                    0, 0, 0) : color(255, 255, 255);
        }
        result.updatePixels();
        return result;
    }

    public PImage intensityThresholding(PApplet app, PImage image) {
        this.init();
        int threshold = 128;

        PImage result = createImage(image.width, image.height, HSB);
        int[] imagePixels = image.pixels;
        int[] resultPixels = result.pixels;

        for (int i = 0; i < imagePixels.length; ++i) {
            if (app.brightness(imagePixels[i]) >= threshold) {
                resultPixels[i] = color(255);
            } else {
                resultPixels[i] = color(0);
            }
        }
        result.updatePixels();
        return result;
    }

    public PImage HSBfiltering(PApplet app, PImage img) {
        init();
        PImage result = createImage(img.width, img.height, RGB);
        img.loadPixels();
        // result
        for (int i = 0; i < img.width * img.height; i++) {
            float currentBright = app.brightness(img.pixels[i]);
            float currentSat = app.saturation(img.pixels[i]);
            float currentHue = app.hue(img.pixels[i]);
            // Threshold
            if (minHue <= currentHue && currentHue <= maxHue &&
                    minSat <= currentSat && currentSat <= maxSat &&
                    minBright <= currentBright && currentBright <= maxBright) {
                result.pixels[i] = color(255);
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
     * @param img    - the image to be convoluted
     * @param Kernel - the Kernel to convolute the image with
     * @return a new <tt>PImage</tt>, result of the normalized convolution of
     * the image with the kernel passed as parameters.
     */
    public PImage convolutionNormalized(PImage img, int[][] Kernel) {
        int normalizingWeight = sumWeight(Kernel);
        return convolutionWithWeight(img, Kernel, normalizingWeight);
    }

    public PImage blur(PApplet app, PImage image) {
        return convolutionNormalized(image, BoxBlurKernel);
    }

    private int sumWeight(int[][] Kernel) {
        int sum = 0;
        for (int i = 0; i < Kernel.length; i++) {
            for (int j = 0; j < Kernel[i].length; j++)
                sum += Kernel[i][j];
        }
        return sum;
    }

    /**
     * Performs a simple normalized convolution operation on the given image,
     * using the specified weight.
     *
     * @param img    - the image to be convoluted
     * @param Kernel - the Kernel to convolute the image with
     * @param weight - the divisor of all summed intensities
     * @return a new <tt>PImage</tt>, result of the convolution of the image
     * with the kernel passed as parameters. <br>
     * <b>Warning :</b> The borders of the image may seem darken.
     */
    public PImage convolutionWithWeight(PImage img, int[][] Kernel, int weight) {
        this.init();
        PImage result = createImage(img.width, img.height, RGB);

        for (int x = Kernel.length / 2; x < img.width - Kernel.length / 2; x++) {
            for (int y = Kernel.length / 2; y < img.height - Kernel.length / 2; y++) {
                int reds = 0;
                int greens = 0;
                int blues = 0;

                int rasterPosition = x + y * img.width;

                for (int i = -Kernel.length / 2; i <= Kernel.length / 2; i++) {
                    for (int j = -Kernel.length / 2; j <= Kernel.length / 2; j++) {
                        reds += red(img.pixels[rasterPosition + i * img.width
                                + j])
                                * Kernel[i + Kernel.length / 2][j
                                + Kernel.length / 2];
                        blues += blue(img.pixels[rasterPosition + i * img.width
                                + j])
                                * Kernel[i + Kernel.length / 2][j
                                + Kernel.length / 2];
                        greens += green(img.pixels[rasterPosition + i
                                * img.width + j])
                                * Kernel[i + Kernel.length / 2][j
                                + Kernel.length / 2];
                    }
                }

                result.pixels[rasterPosition] = color(reds / weight, greens
                        / weight, blues / weight);
            }
        }
        result.updatePixels();
        return result;
    }

    /**
     * Performs an edge detection operation (using Sobel operator) on the given
     * image.
     *
     * @param img       - the image for which we want to detect edges
     * @param threshold - intensity of the edge detection, between 0 and 1
     * @return a new ALPHA <tt>PImage</tt>, result of the Sobel edge detection
     * algorithm on the given image.
     */
    public PImage sobel(PApplet app, PImage img, double threshold) {
        init();
        PImage gray = new PImage(img.width, img.height);
        gray.pixels = img.pixels.clone();
        gray.filter(GRAY); // If the image passed as parameter is not B&W,


        // Assignment Kernels :
        int[][] hKernel = {{0, 1, 0}, {0, 0, 0}, {0, -1, 0}};
        int[][] vKernel = {{0, 0, 0}, {1, 0, -1}, {0, 0, 0}};

        // Alternative Kernels :
        // int[][] hKernel = {{-1,-2,-1},{0,0,0},{1,2,1}};
        // int[][] vKernel = {{-1,0,1},{-2,0,2},{-1,0,1}};

        PImage result = createImage(gray.width, gray.height, ALPHA);
        result.loadPixels();
        for (int i = 0; i < gray.width * gray.height; i++) { // Clear the image
            result.pixels[i] = app.color(0);
        }

        double max = 0;
        double[] buffer = new double[gray.width * gray.height];

        // Does the double convolution :
        int N = hKernel.length;

        for (int x = N / 2; x < gray.width - N / 2; x++) {
            for (int y = N / 2; y < gray.height - N / 2; y++) {
                float sum_h = 0, sum_v = 0;
                for (int i = -N / 2; i <= N / 2; i++) {
                    for (int j = -N / 2; j <= N / 2; j++) {
                        sum_h += hKernel[i + (N / 2)][j + (N / 2)]
                                * app.brightness(gray.pixels[x + y * gray.width + i
                                + j * gray.width]);
                        sum_v += vKernel[i + (N / 2)][j + (N / 2)]
                                * app.brightness(gray.pixels[x + y * gray.width + i
                                + j * gray.width]);
                    }
                }
                buffer[x + y * gray.width] = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
                if (sqrt(pow(sum_h, 2) + pow(sum_v, 2)) > max) {
                    max = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
                }
            }
        }

        for (int y = 2; y < gray.height - 2; y++) { // Skip top and bottom edges
            for (int x = 2; x < gray.width - 2; x++) { // Skip left and right
                if (buffer[y * gray.width + x] > (int) (max * threshold)) {
                    result.pixels[y * gray.width + x] = color(255);
                } else {
                    result.pixels[y * gray.width + x] = color(0);
                }
            }
        }

        result.updatePixels();
        return result;
    }

    /**
     * @param edgeImg - original <tt>PImage</tt> we want to detect the edges on.
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
                if (this.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    // determine and store all possible (r, phi) pairs
                    // describing lines going through the point.
                    // convert (r, phi) to coordinates in the accumulator
                    for (int phi = 0; phi < phiDim; phi++) {
                        float realPhi = phi * discretizationStepsPhi;
                        double r = x * Math.cos(realPhi) + y
                                * Math.sin(realPhi);
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
     * @param edgeImg     - original <tt>PImage</tt> we want to detect the edges on.
     * @param accumulator - the int[] resulting of <tt>hough</tt>
     * @param nLines      - number of lines we want to take
     * @return a new <tt>ArrayList</tt> of PVector with nLines tuples r, phi.
     */
    ArrayList<PVector> getCoordinates(PImage edgeImg, int[] accumulator,
                                      int nLines) {
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
                            int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
                                    + accR + dR + 1;
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
     * @param edgeImg     - original <tt>PImage</tt> we want to detect the edges on.
     * @param accumulator - the int[] resulting of <tt>hough</tt>
     * @return a new <tt>PImage</tt> with the accumulator resized to the edgeImg
     * size.
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
     * @param lines - <tt>ArrayList</tt> of PVector of tuple r, phi, result of
     *              getCoordinates.
     * @return a new <tt>ArrayList</tt> containt the carthesian coordinates of
     * the intersection of the different lines in <tt>lines</tt>
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

    private PVector intersection(PVector line1, PVector line2) {
        float r1 = line1.x, r2 = line2.x, p1 = line1.y, p2 = line2.y;
        float d = cos(p2) * sin(p1) - cos(p1) * sin(p2);

        int x = (int) ((r2 * sin(p1) - r1 * sin(p2)) / d);
        int y = (int) ((-r2 * cos(p1) + r1 * cos(p2)) / d);

        return new PVector(x, y);
    }

    /**
     * @param takenLine - <tt>ArrayList</tt> of PVector of (r, phi), result of
     *                  getCoordinates.
     * @param baseImg   - <tt>PImage</tt> to display under the lines.
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
            int x2 = baseImg.width;
            int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
            int y3 = baseImg.width;
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
     * @param interCoords - <tt>ArrayList</tt> of PVector (x,y(,z=0)), result of
     *                    getIntersection.
     * @param baseImage   - <tt>PImage</tt> to display under the dots.
     * @return a new <tt>PImage</tt> with dots corresponding on it.
     */
    PGraphics drawIntersection(ArrayList<PVector> interCoords, PImage baseImage) {
        PGraphics intersection = createGraphics(baseImage.width,
                baseImage.height);
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

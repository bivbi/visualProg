package ch.epfl.cs211;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.*;

/**
 * Created by virgile on 13/06/2015.
 */
public class TangibleGame extends game {

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"TangibleGame"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }

    ImageProcessing imgProc;
    Movie cam;


    public void setup() {
        super.setup();
        imgProc = new ImageProcessing();
        imgProc.getMinMaxBoundaries();
        cam = new Movie(this, "C:\\Users\\virgile\\Documents\\workspace\\localVisual\\week12\\bin\\testvideo.mp4");
        cam.loop();
    }

    public void draw() {
        super.draw();
        PImage image = getCam();
        PVector rot = imgProc.getRotation(this, image);
        if (rot != null) setAngles(rot);
        image(image, 640, 0);
        image(imgProc.doTransformations(this, image), 0, 0);
    }

    private PImage getCam() {
        cam.read();
        return cam.get();
    }
}
